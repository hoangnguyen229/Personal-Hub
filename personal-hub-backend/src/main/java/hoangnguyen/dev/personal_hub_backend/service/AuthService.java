package hoangnguyen.dev.personal_hub_backend.service;

import hoangnguyen.dev.personal_hub_backend.dto.request.LoginRequest;
import hoangnguyen.dev.personal_hub_backend.dto.request.RegisterRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.AuthResponse;

/**
 * Service interface for authentication-related operations
 * Handles user registration, login, OAuth integration, and password management
 */
public interface AuthService {
    /**
     * Register a new user
     * 
     * @param request registration details
     * @return authentication response with tokens
     */
    AuthResponse register(RegisterRequest request);
    
    /**
     * Authenticate a user with credentials
     * 
     * @param request login credentials
     * @return authentication response with tokens
     */
    AuthResponse login(LoginRequest request);
    
    /**
     * Authenticate or register a user via Google OAuth
     * 
     * @param email user email from Google
     * @param username username from Google
     * @param token optional token
     * @return authentication response with tokens
     */
    AuthResponse googleLogin(String email, String username, String token);
    
    /**
     * Authenticate or register a user via GitHub OAuth
     * 
     * @param email user email from GitHub
     * @param username username from GitHub
     * @param token optional token
     * @return authentication response with tokens
     */
    AuthResponse githubLogin(String email, String username, String token);
    
    /**
     * Initiate password reset process for a user
     * 
     * @param email user email for password reset
     * @return authentication response with status
     */
    AuthResponse forgotPassword(String email);
    
    /**
     * Verify one-time password during reset process
     * 
     * @param email user email
     * @param otp one-time password to verify
     * @return authentication response with reset token
     */
    AuthResponse verifyOTP(String email, String otp);
    
    /**
     * Complete password reset with new password
     * 
     * @param token reset verification token
     * @param newPassword new password to set
     * @return authentication response with status
     */
    AuthResponse resetPassword(String token, String newPassword);
}