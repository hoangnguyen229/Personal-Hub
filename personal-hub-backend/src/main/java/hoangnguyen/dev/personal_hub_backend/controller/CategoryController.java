package hoangnguyen.dev.personal_hub_backend.controller;

import hoangnguyen.dev.personal_hub_backend.dto.request.CategoryRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.CategoryResponse;
import hoangnguyen.dev.personal_hub_backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller responsible for category-related endpoints
 * Handles retrieving category information
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    
    private final CategoryService categoryService;

    /**
     * Create category
     *
     * @return List of category responses
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> addCategory(@RequestBody CategoryRequest categoryRequest) {
        CategoryResponse category = categoryService.addCategory(categoryRequest);
        return ResponseEntity.ok(category);
    }
    
    /**
     * Get all categories
     * 
     * @return List of category responses
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Get a specific category by its slug
     * 
     * @param slug the unique slug identifier for the category
     * @return Category response with matching slug
     */
    @GetMapping("/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        CategoryResponse category = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(category);
    }
}