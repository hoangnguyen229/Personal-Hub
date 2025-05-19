package hoangnguyen.dev.personal_hub_backend.controller;

import hoangnguyen.dev.personal_hub_backend.dto.request.*;
import hoangnguyen.dev.personal_hub_backend.dto.response.AuthResponse;
import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import hoangnguyen.dev.personal_hub_backend.exception.ApiException;
import hoangnguyen.dev.personal_hub_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller responsible for authentication-related endpoints
 * Handles user registration, login, OAuth callbacks, and password management
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * Register a new user
     * 
     * @param registerRequest contains user registration details
     * @return AuthResponse with registration status and tokens
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse authResponse = authService.register(registerRequest);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Authenticate a user with credentials
     * 
     * @param loginRequest contains login credentials
     * @return AuthResponse with login status and tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Process Google OAuth callback
     * 
     * @param email user email from Google
     * @param username user name from Google
     * @param status authentication status
     * @param error error message if auth failed
     * @return AuthResponse or error response
     */
    @GetMapping("/google/callback")
    public ResponseEntity<?> googleCallback(
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String error

    ) {
        if("failed".equals(status)) {
            return ResponseEntity.status(401).body(Map.of(
                    "message", "Google login failed!",
                    "error", error
            ));
        }
        AuthResponse authResponse = authService.googleLogin(email, username, null);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Process GitHub OAuth callback
     * 
     * @param email user email from GitHub
     * @param username user name from GitHub
     * @param status authentication status
     * @param error error message if auth failed
     * @return AuthResponse or error response
     */
    @GetMapping("/github/callback")
    public ResponseEntity<?> githubCallback(
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String error

    ) {
        if("failed".equals(status)) {
            return ResponseEntity.status(401).body(Map.of(
                    "message", "Github login failed!",
                    "error", error
            ));
        }
        AuthResponse authResponse = authService.githubLogin(email, username, null);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Initiate password reset process
     * 
     * @param request containing user email
     * @return AuthResponse with password reset status
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@RequestBody ForgotPasswordRequest request){
        AuthResponse authResponse = authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Verify OTP code during password reset
     * 
     * @param request containing email and OTP code
     * @return AuthResponse with verification status
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOTP(@RequestBody VerifyOTPRequest request){
        AuthResponse authResponse = authService.verifyOTP(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Complete password reset with new password
     * 
     * @param request containing reset token and new password details
     * @return AuthResponse with reset status
     * @throws ApiException if passwords don't match
     */
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody ResetPasswordRequest request){
        if(!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApiException(ErrorCodeEnum.PASSWORD_DO_NOT_MATCH);
        }
        AuthResponse authResponse = authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(authResponse);
    }
}