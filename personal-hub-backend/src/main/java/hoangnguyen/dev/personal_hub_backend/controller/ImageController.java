package hoangnguyen.dev.personal_hub_backend.controller;

import hoangnguyen.dev.personal_hub_backend.dto.response.ImageResponse;
import hoangnguyen.dev.personal_hub_backend.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/image")
public class ImageController {
    private final ImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> uploadImage(@RequestParam("images") MultipartFile images) {
        String imageUrl = imageService.uploadImage(images);
        ImageResponse imageResponse = ImageResponse.builder()
                .imageUrl(imageUrl)
                .build();
        return ResponseEntity.ok(imageResponse);
    }
}
