package hoangnguyen.dev.personal_hub_backend.service;

import hoangnguyen.dev.personal_hub_backend.dto.request.CategoryRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.CategoryResponse;

import java.util.List;

/**
 * Service interface for category-related operations
 * Handles retrieving and managing blog post categories
 */
public interface CategoryService {

    /**
     * Creates a new category
     *
     * @param categoryRequest the post details
     * @return the created post response
     */
    CategoryResponse addCategory(CategoryRequest categoryRequest);

    /**
     * Retrieves all categories
     * 
     * @return List of all categories
     */
    List<CategoryResponse> getAllCategories();
    
    /**
     * Retrieves a category by its slug
     * 
     * @param slug The slug of the category
     * @return The category with the given slug
     */
    CategoryResponse getCategoryBySlug(String slug);
}