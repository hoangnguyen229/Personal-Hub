package hoangnguyen.dev.personal_hub_backend.service.impl;

import hoangnguyen.dev.personal_hub_backend.dto.request.CategoryRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.CategoryResponse;
import hoangnguyen.dev.personal_hub_backend.entity.Category;
import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import hoangnguyen.dev.personal_hub_backend.exception.ApiException;
import hoangnguyen.dev.personal_hub_backend.repository.CategoryRepository;
import hoangnguyen.dev.personal_hub_backend.service.CategoryService;
import hoangnguyen.dev.personal_hub_backend.uitls.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the CategoryService interface
 * Handles operations related to blog post categories
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse addCategory(CategoryRequest categoryRequest) {
        if (categoryRepository.existsCategoryByCategoryName(categoryRequest.getCategoryName())) {
            throw new ApiException(ErrorCodeEnum.CATEGORY_ALREADY_EXISTS);
        }

        Category category = Category.builder()
                .categoryName(categoryRequest.getCategoryName())
                .slug(SlugUtils.toSlug(categoryRequest.getCategoryName()))
                .build();

        category = categoryRepository.save(category);

        return mapToCategoryResponse(category);
    }

    /**
     * Retrieves all available categories
     * 
     * @return List of all categories as CategoryResponse objects
     */
    @Override
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific category by its slug
     * 
     * @param slug The unique slug identifier for the category
     * @return The category as a CategoryResponse object
     * @throws ApiException if the category is not found
     */
    @Override
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.CATEGORY_NOT_FOUND));
        
        return mapToCategoryResponse(category);
    }
    
    /**
     * Maps a Category entity to a CategoryResponse DTO
     * 
     * @param category The Category entity to map
     * @return The CategoryResponse DTO
     */
    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .categoryID(category.getCategoryID())
                .name(category.getCategoryName())
                .slug(category.getSlug())
                .build();
    }
}
