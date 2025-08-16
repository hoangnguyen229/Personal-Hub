package hoangnguyen.dev.personal_hub_backend.config;

import hoangnguyen.dev.personal_hub_backend.entity.Post;
import hoangnguyen.dev.personal_hub_backend.repository.PostRepository;
import hoangnguyen.dev.personal_hub_backend.service.PostDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostSyncInitializer implements CommandLineRunner {

   private final PostRepository postRepository;
   private final PostDocumentService postDocumentService;

   @Override
   public void run(String... args) throws Exception {
       log.info("Bắt đầu đồng bộ dữ liệu từ database sang Elasticsearch...");

       // Lấy tất cả bài viết từ database
       List<Post> posts = postRepository.findAllWithTags();

       // Đồng bộ từng bài viết sang Elasticsearch
       for (Post post : posts) {
           try {
               postDocumentService.syncPostToElasticsearch(post);
//                log.info("Đồng bộ bài viết với postID={} thành công", post.getPostID());
           } catch (Exception e) {
               log.error("Lỗi khi đồng bộ bài viết với postID={}: {}", post.getPostID(), e.getMessage());
           }
       }

       log.info("Hoàn tất đồng bộ dữ liệu. Tổng số bài viết: {}", posts.size());
   }
}