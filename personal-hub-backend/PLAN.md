# Post CRUD API with Cloudinary Integration Plan

## Overview
This document outlines the plan for implementing a REST API for CRUD operations on blog posts with Cloudinary integration for image storage in the Personal Hub application.

## Current State
The application already has the following entity models:
- Post: Contains blog post details like title, content, excerpt, slug, timestamps
- Image: Connected to Post for storing image URLs
- Comment: For post comments
- Category: For categorizing posts
- Tag: For tagging posts

## Implementation Plan

### 1. Cloudinary Setup (Week 1)
- [ ] Add Cloudinary dependencies to pom.xml
- [ ] Create Cloudinary configuration class
- [ ] Implement service for image upload/delete operations

### 2. DTOs and Request/Response Models (Week 1)
- [ ] Create PostDTO for data transfer
- [ ] Create request models: CreatePostRequest, UpdatePostRequest
- [ ] Create response models: PostResponse, PostDetailResponse
- [ ] Implement DTO mappers

### 3. Repository Layer (Week 1)
- [ ] Implement/extend PostRepository
- [ ] Add custom query methods for filtering and searching posts

### 4. Service Layer (Week 2)
- [ ] Implement PostService interface and implementation
- [ ] Add CRUD operations
- [ ] Implement slug generation logic
- [ ] Add service methods for image upload and management
- [ ] Implement pagination and filtering

### 5. Controller Layer (Week 2)
- [ ] Create PostController with REST endpoints
- [ ] Implement request validation
- [ ] Add proper response status codes and error handling
- [ ] Document API with Swagger/OpenAPI

### 6. Security (Week 3)
- [ ] Add authorization rules for post operations
- [ ] Implement owner-based access control
- [ ] Configure CORS settings

### 7. Testing (Week 3)
- [ ] Unit tests for services
- [ ] Integration tests for controllers
- [ ] End-to-end testing

## Detailed Tasks

### 1. Cloudinary Setup

#### Dependencies
Add to pom.xml:
```xml
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http44</artifactId>
    <version>1.35.0</version>
</dependency>
```

#### Configuration
Create CloudinaryConfig class:
```java
@Configuration
public class CloudinaryConfig {
    @Value("${cloudinary.cloud-name}")
    private String cloudName;
    
    @Value("${cloudinary.api-key}")
    private String apiKey;
    
    @Value("${cloudinary.api-secret}")
    private String apiSecret;
    
    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        return new Cloudinary(config);
    }
}
```

Add properties to application.properties:
```
cloudinary.cloud-name=your-cloud-name
cloudinary.api-key=your-api-key
cloudinary.api-secret=your-api-secret
```

### 2. Create Service for Image Handling

```java
@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;
    
    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }
    
    public String uploadImage(MultipartFile file) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("folder", "personal-hub/posts");
        
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
        return (String) uploadResult.get("secure_url");
    }
    
    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
```

### 3. DTOs and Request/Response Models

#### CreatePostRequest
```java
public class CreatePostRequest {
    private String title;
    private String content;
    private String excerpt;
    private Integer categoryId;
    private List<Integer> tagIds;
    // Getter/Setter/Constructor
}
```

#### UpdatePostRequest
```java
public class UpdatePostRequest {
    private String title;
    private String content;
    private String excerpt;
    private Integer categoryId;
    private List<Integer> tagIds;
    // Getter/Setter/Constructor
}
```

#### PostResponse (for listing)
```java
public class PostResponse {
    private Long id;
    private String title;
    private String excerpt;
    private String slug;
    private String thumbnailUrl;
    private String authorName;
    private LocalDateTime createdAt;
    private String categoryName;
    // Getter/Setter/Constructor
}
```

#### PostDetailResponse
```java
public class PostDetailResponse {
    private Long id;
    private String title;
    private String content;
    private String slug;
    private List<String> imageUrls;
    private AuthorDTO author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String categoryName;
    private List<String> tags;
    private List<CommentDTO> comments;
    // Getter/Setter/Constructor
}
```

### 4. Service Implementation

#### PostService Interface
```java
public interface PostService {
    Page<PostResponse> getAllPosts(int page, int size, String sortBy, String categorySlug, String tag);
    PostDetailResponse getPostById(Long id);
    PostDetailResponse getPostBySlug(String slug);
    PostDetailResponse createPost(CreatePostRequest request, List<MultipartFile> images, Long userId);
    PostDetailResponse updatePost(Long id, UpdatePostRequest request, List<MultipartFile> newImages, List<Long> imagesToDelete, Long userId);
    void deletePost(Long id, Long userId);
    String generateSlug(String title);
}
```

### 5. Controller Implementation

```java
@RestController
@RequestMapping("/api/posts")
public class PostController {
    
    private final PostService postService;
    
    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }
    
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag) {
        return ResponseEntity.ok(postService.getAllPosts(page, size, sortBy, category, tag));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PostDetailResponse> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }
    
    @GetMapping("/slug/{slug}")
    public ResponseEntity<PostDetailResponse> getPostBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(postService.getPostBySlug(slug));
    }
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDetailResponse> createPost(
            @RequestPart("post") @Valid CreatePostRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication) {
        CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
        return new ResponseEntity<>(
                postService.createPost(request, images, userDetail.getId()),
                HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDetailResponse> updatePost(
            @PathVariable Long id,
            @RequestPart("post") @Valid UpdatePostRequest request,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @RequestParam(value = "imagesToDelete", required = false) List<Long> imagesToDelete,
            Authentication authentication) {
        CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
        return ResponseEntity.ok(
                postService.updatePost(id, request, newImages, imagesToDelete, userDetail.getId()));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            Authentication authentication) {
        CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();
        postService.deletePost(id, userDetail.getId());
        return ResponseEntity.noContent().build();
    }
}
```

## API Endpoints

| Method | Endpoint | Description | Access Control |
|--------|----------|-------------|---------------|
| GET | /api/posts | Get all posts with pagination and filtering | Public |
| GET | /api/posts/{id} | Get post by ID | Public |
| GET | /api/posts/slug/{slug} | Get post by slug | Public |
| POST | /api/posts | Create a new post | Authenticated |
| PUT | /api/posts/{id} | Update an existing post | Authenticated + Owner |
| DELETE | /api/posts/{id} | Delete a post | Authenticated + Owner |

## Timeline

- Week 1: Set up Cloudinary, create DTOs and repositories
- Week 2: Implement services and controllers
- Week 3: Add security, testing, and finalize documentation

## Conclusion

This plan outlines the approach for implementing a complete REST API for blog posts with Cloudinary integration. By following this structured approach, we'll ensure that the API is well-designed, properly tested, and follows best practices for security and performance.