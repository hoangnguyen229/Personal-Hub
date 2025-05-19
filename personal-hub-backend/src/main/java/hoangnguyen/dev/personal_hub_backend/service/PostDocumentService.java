package hoangnguyen.dev.personal_hub_backend.service;

import hoangnguyen.dev.personal_hub_backend.document.PostDocument;
import hoangnguyen.dev.personal_hub_backend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostDocumentService {
    void syncPostToElasticsearch(Post post);
    List<String> getTitleSuggestions(String prefix);
    Page<PostDocument> searchByTitle(String query, Pageable pageable);
}
