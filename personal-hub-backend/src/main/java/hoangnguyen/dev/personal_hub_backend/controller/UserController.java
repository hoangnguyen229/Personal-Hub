package hoangnguyen.dev.personal_hub_backend.controller;

import hoangnguyen.dev.personal_hub_backend.dto.request.UserRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.UserResponse;
import hoangnguyen.dev.personal_hub_backend.entity.CustomUserDetail;
import hoangnguyen.dev.personal_hub_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getUserById(@AuthenticationPrincipal CustomUserDetail user){
        UserResponse userResponse = userService.getUserById(user.getId());
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(
            @PathVariable String email
    ){
        UserResponse userResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<UserResponse>> getAllUser(){
        List<UserResponse> userResponseList = userService.getAllUsers();
        return ResponseEntity.ok(userResponseList);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> updateUser(
            @ModelAttribute UserRequest userRequest,
            @AuthenticationPrincipal CustomUserDetail userDetail
    ){
        UserResponse userResponse = userService.updateUser(userDetail.getId(), userRequest);
        return ResponseEntity.ok(userResponse);
    }
}
