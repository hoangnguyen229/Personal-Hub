package hoangnguyen.dev.personal_hub_backend.service.impl;

import hoangnguyen.dev.personal_hub_backend.dto.request.LoginRequest;
import hoangnguyen.dev.personal_hub_backend.dto.request.RegisterRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.AuthResponse;
import hoangnguyen.dev.personal_hub_backend.dto.response.UserResponse;
import hoangnguyen.dev.personal_hub_backend.entity.CustomUserDetail;
import hoangnguyen.dev.personal_hub_backend.entity.Role;
import hoangnguyen.dev.personal_hub_backend.entity.User;
import hoangnguyen.dev.personal_hub_backend.entity.VerificationToken;
import hoangnguyen.dev.personal_hub_backend.enums.AuthTypeEnum;
import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import hoangnguyen.dev.personal_hub_backend.enums.RoleTypeEnum;
import hoangnguyen.dev.personal_hub_backend.enums.TokenPurposeEnum;
import hoangnguyen.dev.personal_hub_backend.exception.ApiException;
import hoangnguyen.dev.personal_hub_backend.repository.UserRepository;
import hoangnguyen.dev.personal_hub_backend.service.*;
import hoangnguyen.dev.personal_hub_backend.validator.RegisterValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.ldap.PagedResultsControl;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Implementation of the AuthService interface
 * Handles user authentication, registration, and password management
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final EmailService emailService;
    private final UserStatusService userStatusService;

    /**
     * Registers a new user with email, username and password
     * Validates the request and checks for email availability
     * 
     * @param request the registration details
     * @return authentication response with success message
     */
    @Override
    public AuthResponse register(RegisterRequest request) {
        RegisterValidator.validateRegisterRequest(request);
        checkEmailAvailability(request.getEmail());

        User user = createNewUser(request);
        userRepository.save(user);

        return buildAuthResponse("Registration successful!", null, mapToUserResponse(user));
    }

    /**
     * Authenticates a user with email and password
     * 
     * @param request the login credentials
     * @return authentication response with JWT token
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        User user = findUserByEmail(request.getEmail());
        authenticateUser(request.getEmail(), request.getPassword());

        String jwtToken = generateAndSaveToken(user);
        user.setAuthType(AuthTypeEnum.LOCAL);
        userRepository.save(user);

    //        userStatusService.setUserOnline(user.getUserID());

        return buildAuthResponse("Login successful!", jwtToken, mapToUserResponse(user));
    }

    /**
     * Handles authentication via Google OAuth
     * Creates a new user if the email doesn't exist
     * 
     * @param email user email from Google
     * @param username username from Google
     * @param token optional token
     * @return authentication response with JWT token
     */
    @Override
    @Transactional
    public AuthResponse googleLogin(String email, String username, String token) {
        User user = userRepository.findByEmail(email).orElseGet(
                () -> createAndSaveOAuthUser(email, username, AuthTypeEnum.GOOGLE)
        );

        String jwtToken = token != null ? token : generateAndSaveToken(user);
        user.setAuthType(AuthTypeEnum.GOOGLE);
        userRepository.save(user);

//        userStatusService.setUserOnline(user.getUserID());

        return buildAuthResponse("Google login successful!", jwtToken, mapToUserResponse(user));
    }

    /**
     * Handles authentication via GitHub OAuth
     * Creates a new user if the email doesn't exist
     * 
     * @param email user email from GitHub
     * @param username username from GitHub
     * @param token optional token
     * @return authentication response with JWT token
     */
    @Override
    @Transactional
    public AuthResponse githubLogin(String email, String username, String token) {
        User user = userRepository.findByEmail(email).orElseGet(
                () -> createAndSaveOAuthUser(email, username, AuthTypeEnum.GITHUB)
        );

        String jwtToken = token != null ? token : generateAndSaveToken(user);
        user.setAuthType(AuthTypeEnum.GITHUB);
        userRepository.save(user);

//        userStatusService.setUserOnline(user.getUserID());

        return buildAuthResponse("GitHub login successful!", jwtToken, mapToUserResponse(user));
    }

    /**
     * Initiates password reset by generating and sending OTP
     * 
     * @param email user email for password reset
     * @return authentication response with success message
     */
    @Override
    public AuthResponse forgotPassword(String email) {
        User user = findUserByEmail(email);

        String OTP = String.format("%06d", secureRandom.nextInt(1000000));

        VerificationToken verificationToken = VerificationToken.builder()
                .token(OTP)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .isVerified(false)
                .tokenType(TokenPurposeEnum.OTP_VERIFY)
                .user(user)
                .build();

        tokenService.saveVerificationToken(verificationToken);
        emailService.sendOTPEmail(user, OTP);

        return buildAuthResponse("Password reset OTP has been sent to your email", null, null);
    }

    /**
     * Verifies OTP and generates password reset token
     * 
     * @param email user email
     * @param otp one-time password to verify
     * @return authentication response with password reset token
     */
    @Override
    public AuthResponse verifyOTP(String email, String otp) {
        User user = findUserByEmail(email);
        VerificationToken verificationToken = tokenService.findValidOTP(user, otp, TokenPurposeEnum.OTP_VERIFY).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.INVALID_OTP)
        );

        verificationToken.setVerified(true);
        tokenService.updateVerificationToken(verificationToken);

        String passwordResetToken = UUID.randomUUID().toString();
        VerificationToken resetToken = VerificationToken.builder()
                .token(passwordResetToken)
                .expiryTime(LocalDateTime.now().plusMinutes(15))
                .isVerified(false)
                .tokenType(TokenPurposeEnum.PASSWORD_RESET)
                .user(user)
                .build();
        tokenService.saveVerificationToken(resetToken);

        return buildAuthResponse("OTP verified successfully!", passwordResetToken, null);
    }

    /**
     * Completes password reset process
     * 
     * @param token reset verification token
     * @param newPassword new password to set
     * @return authentication response with success message
     */
    @Override
    public AuthResponse resetPassword(String token, String newPassword) {
        VerificationToken resetPasswordToken = tokenService.findValidToken(token, TokenPurposeEnum.PASSWORD_RESET).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.INVALID_TOKEN_RESET_PASSWORD)
        );

        User user = resetPasswordToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetPasswordToken.setVerified(true);
        tokenService.updateVerificationToken(resetPasswordToken);

        return buildAuthResponse("Password reset successful!", null, null);
    }

    @Override
    public AuthResponse logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetail customUserDetail)){
            throw new ApiException(ErrorCodeEnum.UNAUTHORIZED_OPERATION);
        }

        User user = userRepository.findById(customUserDetail.getId()).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );
        tokenService.revokeAllUserTokens(user);
        userStatusService.setUserOffline(user.getUserID());
        SecurityContextHolder.clearContext();
        return buildAuthResponse("Logout successful!", null, null);
    }

    /**
     * Checks if an email is already registered
     * 
     * @param email the email to check
     * @throws ApiException if email already exists
     */
    private void checkEmailAvailability(String email) {
        if(userRepository.findByEmail(email).isPresent()) {
            throw new ApiException(ErrorCodeEnum.EMAIL_ALREADY_EXISTS);
        }
    }

    /**
     * Creates a new user entity from registration request
     * 
     * @param request the registration details
     * @return the created user entity (not yet persisted)
     */
    private User createNewUser(RegisterRequest request){
        Role role = Role.builder()
                .roleID(RoleTypeEnum.ROLE_USER.getValue())
                .build();

        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .authType(AuthTypeEnum.LOCAL)
                .showOnlineStatus(true)
                .build();
    }

    /**
     * Finds a user by email or throws exception
     * 
     * @param email the email to search by
     * @return the found user
     * @throws ApiException if user not found
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );
    }

    /**
     * Authenticates user with Spring Security
     * 
     * @param email user email
     * @param password user password
     * @throws ApiException if credentials are invalid
     */
    private void authenticateUser(String email, String password) {
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            password
                    )
            );
        }catch (BadCredentialsException exception){
            throw new ApiException(ErrorCodeEnum.INVALID_CREDENTIALS);
        }
    }

    /**
     * Generates JWT token and saves it for the user
     * 
     * @param user the user to generate token for
     * @return the generated JWT token
     */
    private String generateAndSaveToken(User user) {
        CustomUserDetail customUserDetail = new CustomUserDetail(user);
        String jwtToken = jwtService.generateToken(customUserDetail);

        tokenService.revokeAllUserTokens(user);
        tokenService.saveUserToken(user, jwtToken);

        return jwtToken;
    }

    /**
     * Creates and saves a new user from OAuth login
     * 
     * @param email user email from OAuth provider
     * @param username username from OAuth provider
     * @param authType the authentication type (GOOGLE, GITHUB)
     * @return the saved user entity
     */
    private User createAndSaveOAuthUser(String email, String username, AuthTypeEnum authType) {
        Role role = Role.builder()
                .roleID(RoleTypeEnum.ROLE_USER.getValue())
                .build();

        User oauthUser = User.builder()
                .email(email)
                .username(username)
                // OAuth users don't need passwords
                .password(null)
                .role(role)
                .authType(authType)
                .build();

        return userRepository.save(oauthUser);
    }

    /**
     * Maps User entity to UserResponse DTO
     * 
     * @param user the user entity to map
     * @return the user response DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userID(user.getUserID())
                .email(user.getEmail())
                .username(user.getUsername())
                .bio(user.getBio())
                .profilePic(user.getProfilePic())
                .authType(user.getAuthType().getValue())
                .build();
    }

    /**
     * Builds a standard authentication response
     * 
     * @param message response message
     * @param token JWT token (optional)
     * @param userResponse user information (optional)
     * @return the built authentication response
     */
    private AuthResponse buildAuthResponse(String message, String token, UserResponse userResponse) {
        return AuthResponse.builder()
                .message(message)
                .access_token(token)
                .user(userResponse)
                .build();
    }
}