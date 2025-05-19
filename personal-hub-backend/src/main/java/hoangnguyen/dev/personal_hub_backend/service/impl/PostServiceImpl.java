package hoangnguyen.dev.personal_hub_backend.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import hoangnguyen.dev.personal_hub_backend.document.PostDocument;
import hoangnguyen.dev.personal_hub_backend.document.TagDocument;
import hoangnguyen.dev.personal_hub_backend.document.UserDocument;
import hoangnguyen.dev.personal_hub_backend.dto.request.PostRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.*;
import hoangnguyen.dev.personal_hub_backend.entity.*;
import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import hoangnguyen.dev.personal_hub_backend.enums.NotificationTypeEnum;
import hoangnguyen.dev.personal_hub_backend.exception.ApiException;
import hoangnguyen.dev.personal_hub_backend.helper.Indices;
import hoangnguyen.dev.personal_hub_backend.repository.*;
import hoangnguyen.dev.personal_hub_backend.service.ImageService;
import hoangnguyen.dev.personal_hub_backend.service.NotificationService;
import hoangnguyen.dev.personal_hub_backend.service.PostDocumentService;
import hoangnguyen.dev.personal_hub_backend.service.PostService;
import hoangnguyen.dev.personal_hub_backend.uitls.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ImageService imageService;
    private final CommentRepository commentRepository;
    private final ImageRepository imageRepository;
    private final FollowRepository followRepository;
    private final NotificationService notificationService;
    private final PostDocumentRepository postDocumentRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final PostDocumentService postDocumentService;


    private static final int MAX_TAG_LENGTH = 50;
    private static final int MAX_TAGS_PER_POST = 10;
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#([\\p{L}0-9_]+)");

    @Override
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        return postRepository.findAllByDeletedAtIsNull(pageable)
                .map(this::mapToPostResponse);
    }

    @Override
    public PostResponse getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.POST_NOT_FOUND));
        return mapToPostResponse(post);
    }

    @Override
    public PostResponse getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.POST_NOT_FOUND));
        return mapToPostResponse(post);
    }

    @Override
    public Page<PostResponse> getPostsByCategory(String categorySlug, Pageable pageable) {
        Category category = categoryRepository.findBySlug(categorySlug)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.CATEGORY_NOT_FOUND));

        return postRepository.findByCategory(category, pageable)
                .map(this::mapToPostResponse);
    }



    @Override
    public Page<PostResponse> getPostsByTag(String tagName, Pageable pageable) {
        try {
            SearchRequest searchRequest = SearchRequest.of(sr -> sr
                    .index(Indices.POST_INDEX)
                    .query(q -> q
                            .bool(b -> b
                                    .must(m -> m
                                            .nested(n -> n
                                                    .path("tags")
                                                    .query(nq -> nq
                                                            .match(ma -> ma
                                                                    .field("tags.tagName")
                                                                    .query(tagName)
                                                                    .analyzer("vi_analyzer")))))
                                    .mustNot(mn -> mn
                                            .exists(e -> e
                                                    .field("deletedAt")))))
                    .from((int) pageable.getOffset())
                    .size(pageable.getPageSize())
            );

            System.out.println("Search request: " + searchRequest);

            SearchResponse<PostDocument> searchResponse = elasticsearchClient.search(searchRequest, PostDocument.class);
            Set<Long> postIds = searchResponse.hits().hits().stream()
                    .map(hit -> hit.source().getPostID())
                    .collect(Collectors.toSet());

            Page<Post> posts = postRepository.findByPostIDIn(postIds, pageable);

            return posts.map(this::mapToPostResponse);
        } catch (Exception e) {
            throw new ApiException(ErrorCodeEnum.SEARCH_FAILED);
        }
    }

    @Override
    public PostResponse getPostsByTitle(String title) {
        Post post = postRepository.findByTitle(title).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.POST_NOT_FOUND)
        );
        return mapToPostResponse(post);
    }

    @Override
    @Transactional
    public PostResponse createPost(PostRequest postRequest, Long userID) {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND));

        Category category = categoryRepository.findById(postRequest.getCategoryID())
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.CATEGORY_NOT_FOUND));

        Set<Tag> tags = processPostTags(postRequest);

        Set<Like> likes = new HashSet<>();

        String slug = SlugUtils.toSlug(postRequest.getTitle());

        if(postRepository.findBySlug(slug).isPresent()) {
            throw new ApiException(ErrorCodeEnum.POST_ALREADY_EXISTS);
        }

        Post post = Post.builder()
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .slug(slug)
                .user(user)
                .category(category)
                .tags(tags)
                .likes(likes)
                .build();

        Post savedPost = postRepository.save(post);

        List<Image> images = processPostImages(postRequest.getContent(), savedPost);
        savedPost.setImages(images);

        postDocumentService.syncPostToElasticsearch(savedPost);

        List<Follow> follows = followRepository.findAllByFollowingUserIDAndDeletedAtIsNull(userID);
        for(Follow follow : follows) {
            User follower = follow.getFollower();
            if(!follower.getUserID().equals(userID)) {
                notificationService.sendNotification(
                        user.getUsername(),
                        follower.getEmail(),
                        savedPost.getPostID(),
                        NotificationTypeEnum.POST
                );
            }
        }

        return mapToPostResponse(savedPost);
    }

//    @Override
//    @Transactional
//    public PostResponse updatePost(Long postId, PostRequest postRequest, Long userId) {
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new ApiException(ErrorCodeEnum.POST_NOT_FOUND));
//
//        if (post.getDeletedAt() != null) {
//            throw new ApiException(ErrorCodeEnum.POST_ALREADY_DELETED);
//        }
//
//        if (!post.getUser().getUserID().equals(userId)) {
//            throw new ApiException(ErrorCodeEnum.UNAUTHORIZED_OPERATION);
//        }
//
//        Category category = categoryRepository.findById(postRequest.getCategoryID())
//                .orElseThrow(() -> new ApiException(ErrorCodeEnum.CATEGORY_NOT_FOUND));
//
//        String newSlug = SlugUtils.toSlug(postRequest.getTitle());
//        if (!post.getSlug().equals(newSlug) && postRepository.findBySlug(newSlug).isPresent()) {
//            throw new ApiException(ErrorCodeEnum.POST_ALREADY_EXISTS);
//        }
//        post.setTitle(postRequest.getTitle());
//        post.setSlug(newSlug);
//        post.setContent(postRequest.getContent());
//        post.setCategory(category);
//
//        Set<Tag> tags = processPostTags(postRequest);
//        post.setTags(tags);
//
////        updatePostImages(post, postRequest.getContent());
//
//        post.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
//
//        Post updatedPost = postRepository.save(post);
//
//        return mapToPostResponse(updatedPost);
//    }

    @Override
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.POST_NOT_FOUND)
        );

        if(!post.getUser().getUserID().equals(userId)) {
            throw new ApiException(ErrorCodeEnum.UNAUTHORIZED_OPERATION);
        }

        if(post.getDeletedAt() != null){
            throw new ApiException(ErrorCodeEnum.POST_ALREADY_DELETED);
        }

        post.setDeletedAt(new Timestamp(System.currentTimeMillis()));
        postRepository.save(post);
    }

    @Override
    public List<PostResponse> getPostByUserId(Long userId) {
        return postRepository.findByUserUserIDAndDeletedAtIsNull(userId)
                .stream()
                .map(this::mapToPostResponse)
                .toList();
    }

    @Override
    public Set<String> autocompleteTags(String prefix) {
        try {
            if(prefix == null || prefix.isEmpty()){
                return Collections.emptySet();
            }

            SearchRequest searchRequest = SearchRequest.of(sr -> sr
                    .index("posts")
                    .query(q -> q
                            .nested(n -> n
                                    .path("tags")
                                    .query(nq -> nq
                                            .prefix(p -> p
                                                    .field("tags.tagName")
                                                    .value(prefix))))) // query: lọc ra các documents có ít nhất một tag thỏa prefix
                    .aggregations("tag_names", a -> a
                            .nested(n -> n
                                    .path("tags"))
                            .aggregations("filtered_tags", fa -> fa
                                    .filter(f -> f
                                            .prefix(p -> p
                                                    .field("tags.tagName")
                                                    .value(prefix))) // aggregations: chỉ thống kê các tag thỏa prefix
                                    .aggregations("tag_terms", at -> at
                                            .terms(t -> t
                                                    .field("tags.tagName.keyword")
                                                    .size(10)))))
            );

            System.out.println("Search request: " + searchRequest);


            SearchResponse<Void> response = elasticsearchClient.search(searchRequest, Void.class);
            System.out.println(response);

            if (response.aggregations() == null || !response.aggregations().containsKey("tag_names")) {
                return Collections.emptySet();
            }

            Set<String> suggestions = response.aggregations()
                    .get("tag_names")
                    .nested()
                    .aggregations()
                    .get("filtered_tags")
                    .filter()
                    .aggregations()
                    .get("tag_terms")
                    .sterms()
                    .buckets()
                    .array()
                    .stream()
                    .map(bucket -> bucket.key().stringValue())
                    .collect(Collectors.toSet());

            return suggestions;
        } catch (Exception e) {
            throw new ApiException(ErrorCodeEnum.SEARCH_FAILED);
        }
    }

    @Override
    public List<String> autocompleteTitles(String prefix) {
        return postDocumentService.getTitleSuggestions(prefix);
    }

    @Override
    public Page<PostResponse> searchByTitle(String query, Pageable pageable) {
        return postDocumentService.searchByTitle(query, pageable)
                .map(postDocument -> {
                    Post post = postRepository.findById(postDocument.getPostID())
                            .orElseThrow(() -> new ApiException(ErrorCodeEnum.POST_NOT_FOUND));
                    return mapToPostResponse(post);
                });
    }

//    public void syncPostToElasticsearch(Post post){
//        PostDocument postDocument = PostDocument.builder()
//                .postID(post.getPostID())
//                .title(post.getTitle())
//                .content(post.getContent())
//                .slug(post.getSlug())
//                .categoryID(post.getCategory().getCategoryID())
//                .createdAt(post.getCreatedAt())
//                .updatedAt(post.getUpdatedAt())
//                .deletedAt(post.getDeletedAt())
//                .user(UserDocument.builder()
//                        .userID(post.getUser().getUserID())
//                        .username(post.getUser().getUsername())
//                        .email(post.getUser().getEmail())
//                        .build())
//                .tags(post.getTags().stream()
//                        .map(tag -> TagDocument.builder()
//                                .tagID(tag.getTagID())
//                                .tagName(tag.getTagName())
//                                .slug(tag.getSlug())
//                                .build())
//                        .collect(Collectors.toSet()))
//                .build();
//
//        postDocumentRepository.save(postDocument);
//    }


    /**
     * Update post images
     */
//    private void updatePostImages(Post post, String content) {
//        // Lấy danh sách URL hình ảnh mới từ nội dung
//        Set<String> newImageUrls = imageService.extractImageUrls(content);
//
//        // Lấy danh sách hình ảnh hiện tại của bài viết
//        List<Image> currentImages = post.getImages() != null ? post.getImages() : new ArrayList<>();
//
//        // Xóa các hình ảnh không còn được sử dụng
//        List<Image> imagesToRemove = currentImages.stream()
//                .filter(image -> !newImageUrls.contains(image.getImageUrl()))
//                .collect(Collectors.toList());
//        imagesToRemove.forEach(image -> {
//            imageRepository.delete(image);
////            imageService.deleteImage(image.getImageUrl()); // Xóa file vật lý nếu cần
//        });
//
//        // Thêm hoặc cập nhật hình ảnh mới
//        List<Image> updatedImages = new ArrayList<>();
//        for (String imageUrl : newImageUrls) {
//            if (imageService.processImageFromTemp(imageUrl)) {
//                // Kiểm tra xem hình ảnh đã tồn tại trong bài viết chưa
//                Optional<Image> existingImage = currentImages.stream()
//                        .filter(image -> image.getImageUrl().equals(imageUrl))
//                        .findFirst();
//                if (existingImage.isEmpty()) {
//                    Image newImage = new Image();
//                    newImage.setImageUrl(imageUrl);
//                    newImage.setPost(post);
//                    updatedImages.add(imageRepository.save(newImage));
//                } else {
//                    updatedImages.add(existingImage.get());
//                }
//            }
//        }
//
//        // Cập nhật danh sách hình ảnh của bài viết
//        post.setImages(updatedImages);
//    }

    /**
     * Process and save post images
     */
    private List<Image> processPostImages(String content, Post savedPost) {
        Set<String> imageUrls = imageService.extractImageUrls(content);
        List<Image> images = new ArrayList<>();

        for (String imageUrl : imageUrls) {
            if (imageService.processImageFromTemp(imageUrl)) {
                Image image = new Image();
                image.setImageUrl(imageUrl);
                image.setPost(savedPost);
                images.add(imageRepository.save(image));
            }
        }

        return images;
    }

    /**
     * Process tags for the post from both selected tags and tags extracted from content
     */
    private Set<Tag> processPostTags(PostRequest postRequest) {
        Set<Tag> tags = new HashSet<>();

        // Add manually selected tags
        if (!CollectionUtils.isEmpty(postRequest.getTagIDs())) {
            tags.addAll(postRequest.getTagIDs().stream()
                    .map(tagID -> tagRepository.findById(tagID)
                            .orElseThrow(() -> new ApiException(ErrorCodeEnum.TAG_NOT_FOUND)))
                    .collect(Collectors.toSet()));
        }

        // Extract and process hashtags from content
        Set<String> extractedTags = extractHashtags(postRequest.getContent());
        for (String tagName : extractedTags) {
            String normalizedTagName = normalizeTagName(tagName);
            if (!normalizedTagName.isEmpty()) {
                Tag tag = findOrCreateTag(normalizedTagName);
                tags.add(tag);
            }
        }

        if (tags.size() > MAX_TAGS_PER_POST) {
            throw new ApiException(ErrorCodeEnum.TOO_MANY_TAGS);
        }

        return tags;
    }

    /**
     * Extract hashtags from content
     */
    private Set<String> extractHashtags(String content) {
        if (content == null || content.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> hashtags = new HashSet<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(content);

        while (matcher.find()) {
            String hashtag = matcher.group(1);
            if (hashtag.length() <= MAX_TAG_LENGTH) {
                hashtags.add(hashtag);
            }
        }

        return hashtags;
    }

    /**
     * Normalize tag name
     */
    private String normalizeTagName(String tagName) {
        if (tagName.isEmpty() || tagName.length() > MAX_TAG_LENGTH) {
            return "";
        }

        if (!tagName.matches(".*[a-zA-Z0-9].*")) {
            return "";
        }

        return tagName;
    }

    /**
     * Find existing tag or create new one
     */
    private Tag findOrCreateTag(String normalizedTagName) {
        return tagRepository.findByTagName(normalizedTagName)
                .orElseGet(() -> {
                    Tag newTag = Tag.builder()
                            .tagName(normalizedTagName)
                            .slug(SlugUtils.toSlug(normalizedTagName))
                            .build();
                    return tagRepository.save(newTag);
                });
    }

    /**
     * Convert Post entity to PostResponse DTO
     */
    private PostResponse mapToPostResponse(Post post) {
        return PostResponse.builder()
                .postID(post.getPostID())
                .title(post.getTitle())
                .content(post.getContent())
                .slug(post.getSlug())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .deletedAt(post.getDeletedAt())
                .user(mapToUserResponse(post.getUser()))
                .category(mapToCategoryResponse(post.getCategory()))
                .images(mapToImageResponses(post.getImages()))
                .tags(mapToTagResponses(post.getTags()))
                .comments(mapToCommentResponses(post.getComments()))
                .likes(mapToLikeResponses(post.getLikes()))
                .build();
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userID(user.getUserID())
                .email(user.getEmail())
                .username(user.getUsername())
                .bio(user.getBio())
                .profilePic(user.getProfilePic())
                .authType(user.getAuthType().getValue())
                .build();
    }

    /**
     * Convert Category entity to CategoryResponse DTO
     */
    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .categoryID(category.getCategoryID())
                .name(category.getCategoryName())
                .slug(category.getSlug())
                .build();
    }

    /**
     * Convert list of Image entities to ImageResponse DTOs
     */
    private List<ImageResponse> mapToImageResponses(List<Image> images) {
        if (CollectionUtils.isEmpty(images)) {
            return Collections.emptyList();
        }
        return images.stream()
                .map(image -> ImageResponse.builder()
                        .imageUrl(image.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Convert set of Tag entities to TagResponse DTOs
     */
    private Set<TagResponse> mapToTagResponses(Set<Tag> tags) {
        return tags.stream()
                .map(tag -> TagResponse.builder()
                        .name(tag.getTagName())
                        .slug(tag.getSlug())
                        .build())
                .collect(Collectors.toSet());
    }

    private List<CommentResponse> mapToCommentResponses(List<Comment> comments){
        if(CollectionUtils.isEmpty(comments)){
            return Collections.emptyList();
        }
        return comments.stream()
                .filter(comment -> comment.getDeletedAt() == null)
                .map(comment -> CommentResponse.builder()
                        .commentID(comment.getCommentID())
                        .content(comment.getContent())
                        .createdAt(comment.getCreatedAt())
                        .deletedAt(comment.getDeletedAt())
                        .user(mapToUserResponse(comment.getUser()))
                        .build())
                .collect(Collectors.toList());
    }

    private Set<LikeResponse> mapToLikeResponses(Set<Like> likes) {
        return likes.stream()
                .map(like -> LikeResponse.builder()
                        .likeID(like.getLikeID())
                        .userID(like.getUser().getUserID())
                        .postID(like.getPost().getPostID())
                        .createdAt(like.getCreatedAt())
                        .deletedAt(like.getDeletedAt())
                        .build())
                .collect(Collectors.toSet());
    }
}