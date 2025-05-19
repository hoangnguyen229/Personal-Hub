package hoangnguyen.dev.personal_hub_backend.service;

import hoangnguyen.dev.personal_hub_backend.entity.Image;
import hoangnguyen.dev.personal_hub_backend.entity.Post;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * Service interface for image-related operations
 * Handles uploading, storing, and managing images
 */
public interface ImageService {
    /**
     * Upload a single image to storage
     * 
     * @param file the image file to upload
     * @return URL or identifier of the uploaded image
     */
    String uploadImage(MultipartFile file);
    
    /**
     * Save multiple images associated with a post
     * 
     * @param files list of image files to upload and save
     * @param post the post to associate the images with
     * @return list of created image entities
     */
    List<Image> saveImages(List<MultipartFile> files, Post post);

    /**
     * Process image from temporary storage and make it permanent
     *
     * @param imageUrl the URL of the image to process
     * @return true if successful, false otherwise
     */
    boolean processImageFromTemp(String imageUrl);

    /**
     * Extract image URLs from content text
     *
     * @param content the text content to extract from
     * @return set of extracted image URLs
     */
    Set<String> extractImageUrls(String content);

    /**
     * Extract public ID from Cloudinary URL
     *
     * @param imageUrl the Cloudinary URL
     * @return the extracted public ID
     */
    String extractPublicIdFromUrl(String imageUrl);
}