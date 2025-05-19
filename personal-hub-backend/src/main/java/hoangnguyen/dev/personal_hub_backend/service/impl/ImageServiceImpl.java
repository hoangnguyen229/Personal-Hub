package hoangnguyen.dev.personal_hub_backend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import hoangnguyen.dev.personal_hub_backend.entity.Image;
import hoangnguyen.dev.personal_hub_backend.entity.Post;
import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import hoangnguyen.dev.personal_hub_backend.exception.ApiException;
import hoangnguyen.dev.personal_hub_backend.repository.ImageRepository;
import hoangnguyen.dev.personal_hub_backend.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private final Cloudinary cloudinary;
    private final ImageRepository imageRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long TEMP_IMAGE_TTL = 24;
    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile("https?://res\\.cloudinary\\.com/[^\\s\"']+");

    @Override
    public String uploadImage(MultipartFile file) {
        validateFile(file);

        try {
            String uniqueFileName = generateUniqueFileName(file);
            String publicId = "personal-hub-" + uniqueFileName;
            Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "auto"
                )
            );
            String imageUrl = (String) uploadResult.get("secure_url");

            String redisKey = "temp_image:" + publicId;
            redisTemplate.opsForHash().put(redisKey, "url", imageUrl);
            redisTemplate.opsForHash().put(redisKey, "public_id", publicId);
            redisTemplate.expire(redisKey,  TEMP_IMAGE_TTL, TimeUnit.HOURS);

            return imageUrl;
        } catch (IOException e) {
            log.error("Upload image to cloudinary failed", e);
            throw new ApiException(ErrorCodeEnum.IMAGE_UPLOAD_FAILED);
        }
    }

    @Override
    public List<Image> saveImages(List<MultipartFile> files, Post post) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<Image> savedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            String imageUrl = uploadImage(file);
            Image image = new Image();
            image.setImageUrl(imageUrl);
            image.setPost(post);
            savedImages.add(imageRepository.save(image));

            String publicId = extractPublicIdFromUrl(imageUrl);
            redisTemplate.delete("temp_image:" + publicId);
        }

        return savedImages;
    }

    @Override
    public boolean processImageFromTemp(String imageUrl) {
        String publicId = extractPublicIdFromUrl(imageUrl);
        String redisKey = "temp_image:" + publicId;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            redisTemplate.delete(redisKey);
            return true;
        }
        return false;
    }

    @Override
    public Set<String> extractImageUrls(String content) {
        Set<String> imageUrls = new HashSet<>();
        if (content == null || content.isEmpty()) {
            return imageUrls;
        }

        Matcher matcher = IMAGE_URL_PATTERN.matcher(content);
        while (matcher.find()) {
            imageUrls.add(matcher.group());
        }
        return imageUrls;
    }

    @Override
    public String extractPublicIdFromUrl(String imageUrl) {
        String[] parts = imageUrl.split("/");
        String fileName = parts[parts.length - 1];

        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException(ErrorCodeEnum.EMPTY_FILE);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ApiException(ErrorCodeEnum.FILE_TOO_LARGE);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.lastIndexOf(".") == -1) {
            throw new ApiException(ErrorCodeEnum.INVALID_FILE_FORMAT);
        }

        String fileExtension = originalFilename
            .substring(originalFilename.lastIndexOf(".") + 1)
            .toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new ApiException(ErrorCodeEnum.INVALID_FILE_FORMAT);
        }
    }

    private String generateUniqueFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return UUID.randomUUID().toString();
        }

        String fileExtension = originalFilename
            .substring(originalFilename.lastIndexOf(".") + 1)
            .toLowerCase();

        return UUID.randomUUID() + "." + fileExtension;
    }
}