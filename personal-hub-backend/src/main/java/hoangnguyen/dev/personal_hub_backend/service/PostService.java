package hoangnguyen.dev.personal_hub_backend.service;

import hoangnguyen.dev.personal_hub_backend.dto.request.PostRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.PostResponse;
import hoangnguyen.dev.personal_hub_backend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * Service interface for blog post-related operations
 * Handles creating, retrieving, and managing blog posts
 */
public interface PostService {
    /**
     * Creates a new blog post
     * 
     * @param postRequest the post details
     * @param userID the ID of the user creating the post
     * @return the created post response
     */
    PostResponse createPost(PostRequest postRequest, Long userID);
    
    /**
     * Retrieves all posts with pagination
     * 
     * @param pageable pagination information
     * @return page of post responses
     */
    Page<PostResponse> getAllPosts(Pageable pageable);
    
    /**
     * Retrieves a post by its ID
     * 
     * @param postId the ID of the post to retrieve
     * @return the post response
     */
    PostResponse getPostById(Long postId);
    
    /**
     * Retrieves a post by its slug
     * 
     * @param slug the slug of the post to retrieve
     * @return the post response
     */
    PostResponse getPostBySlug(String slug);
    
    /**
     * Retrieves posts filtered by category with pagination
     * 
     * @param categorySlug the slug of the category to filter by
     * @param pageable pagination information
     * @return page of post responses
     */
    Page<PostResponse> getPostsByCategory(String categorySlug, Pageable pageable);
    
    /**
     * Retrieves posts filtered by tag with pagination
     * 
     * @param tagName the name of the tag to filter by
     * @param pageable pagination information
     * @return page of post responses
     */
//    Page<PostResponse> getPostsByTag(String tagName, Pageable pageable);

    /**
     * Retrieves posts filtered by title with pagination
     *
     * @param title the title to filter by
     * @return page of post responses
     */
    PostResponse getPostsByTitle(String title);

//    PostResponse updatePost(Long postId, PostRequest postRequest, Long userId);

    void deletePost(Long postId, Long userId);

    List<PostResponse> getPostByUserId(Long userId);

//    Set<String> autocompleteTags(String prefix);
//    List<String> autocompleteTitles(String prefix);
//    Page<PostResponse> searchByTitle(String query, Pageable pageable);
}
