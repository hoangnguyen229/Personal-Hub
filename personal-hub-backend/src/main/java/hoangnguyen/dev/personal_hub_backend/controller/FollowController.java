package hoangnguyen.dev.personal_hub_backend.controller;

import hoangnguyen.dev.personal_hub_backend.dto.request.FollowRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.FollowResponse;
import hoangnguyen.dev.personal_hub_backend.entity.CustomUserDetail;
import hoangnguyen.dev.personal_hub_backend.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follows")
public class FollowController {
    private final FollowService followService;

    @PostMapping
    public ResponseEntity<FollowResponse> followUser(
            @RequestBody FollowRequest followRequest,
            @AuthenticationPrincipal CustomUserDetail userDetails
    ){
        FollowResponse followResponse = followService.followUser(followRequest, userDetails.getId());
        return ResponseEntity.ok(followResponse);
    }

    @DeleteMapping("/{followingId}")
    public ResponseEntity<FollowResponse> unfollowUser(
            @PathVariable Long followingId,
            @AuthenticationPrincipal CustomUserDetail userDetails
    ){
        FollowResponse followResponse = followService.unfollowUser(followingId, userDetails.getId());
        return ResponseEntity.ok(followResponse);
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<FollowResponse>> getAllFollowersByUserId(
            @PathVariable Long userId
    ){
        List<FollowResponse> followResponses = followService.getAllFollowersByUserId(userId);
        return ResponseEntity.ok(followResponses);
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<List<FollowResponse>> getAllFollowingByUserId(
            @PathVariable Long userId
    ){
        List<FollowResponse> followResponses = followService.getAllFollowingByUserId(userId);
        return ResponseEntity.ok(followResponses);
    }
}
