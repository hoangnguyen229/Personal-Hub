package hoangnguyen.dev.personal_hub_backend.controller;

import hoangnguyen.dev.personal_hub_backend.dto.request.LikeRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.LikeResponse;
import hoangnguyen.dev.personal_hub_backend.entity.CustomUserDetail;
import hoangnguyen.dev.personal_hub_backend.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/likes")
public class LikeController {
    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<LikeResponse> likePost(
            @RequestBody LikeRequest likeRequest,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        LikeResponse likePost = likeService.likePost(likeRequest, userDetail.getId());
        return ResponseEntity.ok(likePost);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<LikeResponse> unLikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ) {
        LikeResponse unlikedPost = likeService.unlikePost(postId, userDetail.getId());
        return ResponseEntity.ok(unlikedPost);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<LikeResponse>> getAllLikesByPostId(
            @PathVariable Long postId
    ) {
        List<LikeResponse> likes = likeService.getAllLikesByPostId(postId);
        return ResponseEntity.ok(likes);
    }

    @GetMapping("/post/{postId}/count")
    public ResponseEntity<Long> getLikeCountByPostId(
            @PathVariable Long postId
    ) {
        long likeCount = likeService.getLikeCountByPostId(postId);
        return ResponseEntity.ok(likeCount);
    }
}
