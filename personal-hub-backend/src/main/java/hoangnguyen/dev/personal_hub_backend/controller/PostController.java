package hoangnguyen.dev.personal_hub_backend.controller;

import hoangnguyen.dev.personal_hub_backend.dto.request.PostRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.PageResponse;
import hoangnguyen.dev.personal_hub_backend.dto.response.PostResponse;
import hoangnguyen.dev.personal_hub_backend.entity.CustomUserDetail;
import hoangnguyen.dev.personal_hub_backend.service.PostService;
import hoangnguyen.dev.personal_hub_backend.uitls.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * Controller responsible for post-related endpoints
 * Handles creating, retrieving, and filtering blog posts
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {
    private final PostService postService;

    /**
     * Create a new post with optional images
     *
     * @param title post title
     * @param content post content
     * @param categoryID category identifier
     * @param tagIDs optional set of tag identifiers
     * @param userDetail authenticated user details
     * @return newly created post response
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("categoryID") Long categoryID,
            @RequestParam(value = "tagIDs", required = false) Set<Long> tagIDs,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        PostRequest postRequest = PostRequest.builder()
                .title(title)
                .content(content)
                .categoryID(categoryID)
                .tagIDs(tagIDs)
                .build();

        PostResponse postResponse = postService.createPost(postRequest, userDetail.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(postResponse);
    }

    /**
     * Get all posts with pagination and sorting
     *
     * @param page page number (zero-based)
     * @param size page size
     * @param sortBy field to sort by
     * @param sortDir sort direction (asc/desc)
     * @return paginated list of posts
     */
    @GetMapping
    public ResponseEntity<PageResponse<PostResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PostResponse> postPage = postService.getAllPosts(pageable);

        PageResponse<PostResponse> response = PageUtils.buildPageResponse(postPage);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific post by its ID
     *
     * @param postId post identifier
     * @return post response with matching ID
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long postId) {
        PostResponse postResponse = postService.getPostById(postId);
        return ResponseEntity.ok(postResponse);
    }

    /**
     * Get a specific post by its slug
     *
     * @param slug post slug identifier
     * @return post response with matching slug
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostResponse> getPostBySlug(@PathVariable String slug) {
        PostResponse postResponse = postService.getPostBySlug(slug);
        return ResponseEntity.ok(postResponse);
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<PostResponse> getPostByTitle(@PathVariable String title) {
        PostResponse postResponse = postService.getPostsByTitle(title);
        return ResponseEntity.ok(postResponse);
    }

    /**
     * Get posts filtered by category
     *
     * @param categorySlug category slug identifier
     * @param page page number (zero-based)
     * @param size page size
     * @param sortBy field to sort by
     * @param sortDir sort direction (asc/desc)
     * @return paginated list of posts in the specified category
     */
    @GetMapping("/category/{categorySlug}")
    public ResponseEntity<PageResponse<PostResponse>> getPostsByCategory(
            @PathVariable String categorySlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PostResponse> postPage = postService.getPostsByCategory(categorySlug, pageable);

        PageResponse<PostResponse> response = PageUtils.buildPageResponse(postPage);
        return ResponseEntity.ok(response);
    }

    /**
     * Get posts filtered by tag
     *
     * @param tagName tag name to filter by
     * @param page page number (zero-based)
     * @param size page size
     * @param sortBy field to sort by
     * @param sortDir sort direction (asc/desc)
     * @return paginated list of posts with the specified tag
     */
   @GetMapping("/tag/{tagName}")
   public ResponseEntity<PageResponse<PostResponse>> getPostsByTag(
           @PathVariable String tagName,
           @RequestParam(defaultValue = "0") int page,
           @RequestParam(defaultValue = "20") int size,
           @RequestParam(defaultValue = "createdAt") String sortBy,
           @RequestParam(defaultValue = "desc") String sortDir
   ) {
       Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ?
               Sort.Direction.ASC : Sort.Direction.DESC;

       Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
       Page<PostResponse> postPage = postService.getPostsByTag(tagName, pageable);

       PageResponse<PostResponse> response = PageUtils.buildPageResponse(postPage);
       return ResponseEntity.ok(response);
   }

    /**
     * Get autocomplete suggestions for tags based on prefix
     *
     * @param prefix the prefix to search for tag suggestions
     * @return list of suggested tag names
     */
   @GetMapping("/tag/autocomplete")
   public ResponseEntity<Set<String>> autocompleteTags(@RequestParam String prefix) {
       Set<String> suggestions = postService.autocompleteTags(prefix);
       return ResponseEntity.ok(suggestions);
   }

    /**
     * Update an existing post
     *
     * @param postId post identifier
     * @param userDetail authenticated user details
     * @return updated post response
     */
//    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<PostResponse> updatePost(
//            @PathVariable Long postId,
//            @RequestParam("title") String title,
//            @RequestParam("content") String content,
//            @RequestParam("categoryID") Long categoryID,
//            @RequestParam(value = "tagIDs", required = false) Set<Long> tagIDs,
//            @RequestParam(value = "images", required = false) List<MultipartFile> images,
//            @AuthenticationPrincipal CustomUserDetail userDetail
//    ) {
//        PostRequest postRequest = PostRequest.builder()
//                .title(title)
//                .content(content)
//                .categoryID(categoryID)
//                .tagIDs(tagIDs)
//                .images(images)
//                .build();

//        PostResponse postResponse = postService.updatePost(postId, postRequest, userDetail.getId());
//        return ResponseEntity.ok(postResponse);
//    }

    /**
     * Delete a post by its ID
     *
     * @param postId post identifier
     * @param userDetail authenticated user details
     * @return response entity with no content
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        postService.deletePost(postId, userDetail.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponse>> getPostByUserId(@PathVariable Long userId) {
        List<PostResponse> postResponses = postService.getPostByUserId(userId);
        return ResponseEntity.ok(postResponses);
    }

   @GetMapping("/suggest")
   public ResponseEntity<List<String>> getTitleSuggestions(@RequestParam String prefix) {
       List<String> suggestions = postService.autocompleteTitles(prefix);
       return ResponseEntity.ok(suggestions);
   }

   @GetMapping("/search")
   public ResponseEntity<PageResponse<PostResponse>> searchPostByTitle(
           @RequestParam String query,
           Pageable pageable
   ){
       Page<PostResponse> postPage = postService.searchByTitle(query, pageable);
       PageResponse<PostResponse> response = PageUtils.buildPageResponse(postPage);
       return ResponseEntity.ok(response);
   }
}