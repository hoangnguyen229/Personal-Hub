package hoangnguyen.dev.personal_hub_backend.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Hit;
import hoangnguyen.dev.personal_hub_backend.document.TagDocument;
import hoangnguyen.dev.personal_hub_backend.document.UserDocument;
import hoangnguyen.dev.personal_hub_backend.dto.response.PostResponse;
import hoangnguyen.dev.personal_hub_backend.document.PostDocument;
import hoangnguyen.dev.personal_hub_backend.entity.Post;
import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import hoangnguyen.dev.personal_hub_backend.exception.ApiException;
import hoangnguyen.dev.personal_hub_backend.helper.Indices;
import hoangnguyen.dev.personal_hub_backend.repository.PostDocumentRepository;
import hoangnguyen.dev.personal_hub_backend.service.PostDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostDocumentImpl implements PostDocumentService {

    private final PostDocumentRepository postDocumentRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;


    @Override
    public void syncPostToElasticsearch(Post post){
        PostDocument.PostDocumentBuilder builder = PostDocument.builder()
                .postID(post.getPostID())
                .title(post.getTitle())
//                .content(post.getContent())
                .slug(post.getSlug())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .deletedAt(post.getDeletedAt())
                .suggest(post.getTitle());

        // Category
        if (post.getCategory() != null) {
            builder.categoryID(post.getCategory().getCategoryID());
        }

        // User
        if (post.getUser() != null) {
            builder.user(UserDocument.builder()
                    .userID(post.getUser().getUserID())
                    .username(post.getUser().getUsername())
                    .email(post.getUser().getEmail())
                    .build());
        }

        // Tags
        if (post.getTags() != null && !post.getTags().isEmpty()) {
            builder.tags(post.getTags().stream()
                    .map(tag -> TagDocument.builder()
                            .tagID(tag.getTagID())
                            .tagName(tag.getTagName())
                            .slug(tag.getSlug())
                            .build())
                    .collect(Collectors.toSet()));
        }

        // Save document
        postDocumentRepository.save(builder.build());
    }


    @Override
    public List<String> getTitleSuggestions(String prefix) {
        try{
            SearchRequest searchRequest = SearchRequest.of(sr -> sr
                    .index(Indices.POST_INDEX)
                    .suggest(s -> s
                            .text(prefix)
                            .suggesters("title-suggest", sug -> sug
                                    .completion(c -> c
                                            .field("suggest")
                                            .skipDuplicates(true)
                                            .size(10))))
            );

            SearchResponse<PostDocument> searchResponse = elasticsearchClient.search(searchRequest, PostDocument.class);

            return searchResponse.suggest()
                    .get("title-suggest")
                    .get(0)
                    .completion()
                    .options()
                    .stream()
                    .map(CompletionSuggestOption::text)
                    .collect(Collectors.toList());
        } catch (Exception ex){
            throw new ApiException(ErrorCodeEnum.SEARCH_FAILED);
        }
    }

    @Override
    public Page<PostDocument> searchByTitle(String query, Pageable pageable) {
        try{
            SearchRequest searchRequest = SearchRequest.of(sr -> sr
                    .index(Indices.POST_INDEX)
                    .query(q -> q
                            .bool(b -> b
                                    .must(m -> m
                                            .match(ma -> ma
                                                    .field("title")
                                                    .query(query)
                                                    .analyzer("vi_analyzer")
                                                    .fuzziness("AUTO")))
                                    .mustNot(mn -> mn
                                            .exists(e -> e
                                                    .field("deletedAt")))))
                    .from((int) pageable.getOffset())
                    .size(pageable.getPageSize())
            );

            System.out.println("Search request: " + searchRequest);

            SearchResponse<PostDocument> searchResponse = elasticsearchClient.search(searchRequest, PostDocument.class);

            System.out.println("Search response: " + searchResponse);
            List<PostDocument> postDocuments = searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .toList();

            long totalHits = searchResponse.hits().total().value();

            return new PageImpl<>(postDocuments, pageable, totalHits);
        } catch (Exception ex){
            throw new ApiException(ErrorCodeEnum.SEARCH_FAILED);
        }
    }
}
