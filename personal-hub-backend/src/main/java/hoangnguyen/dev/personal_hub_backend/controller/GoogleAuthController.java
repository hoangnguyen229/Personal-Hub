package hoangnguyen.dev.personal_hub_backend.controller;

import hoangnguyen.dev.personal_hub_backend.dto.response.AuthResponse;
import hoangnguyen.dev.personal_hub_backend.dto.response.UserResponse;
import hoangnguyen.dev.personal_hub_backend.entity.CustomUserDetail;
import hoangnguyen.dev.personal_hub_backend.entity.Role;
import hoangnguyen.dev.personal_hub_backend.entity.User;
import hoangnguyen.dev.personal_hub_backend.enums.RoleTypeEnum;
import hoangnguyen.dev.personal_hub_backend.repository.UserRepository;
import hoangnguyen.dev.personal_hub_backend.service.AuthService;
import hoangnguyen.dev.personal_hub_backend.service.JwtService;
import hoangnguyen.dev.personal_hub_backend.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller responsible for Google OAuth authentication
 * Handles Google authentication callbacks and user verification
 * Currently empty but prepared for Google OAuth integration
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/google")
public class GoogleAuthController {

}