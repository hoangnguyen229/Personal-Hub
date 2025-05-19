package hoangnguyen.dev.personal_hub_backend.service;

import hoangnguyen.dev.personal_hub_backend.entity.User;
import hoangnguyen.dev.personal_hub_backend.entity.VerificationToken;
import hoangnguyen.dev.personal_hub_backend.enums.TokenPurposeEnum;

import java.util.Optional;

/**
 * Service interface for token-related operations
 * Handles creating, validating, and managing user tokens for authentication and verification
 */
public interface TokenService {
    /**
     * Revokes all existing tokens for a specific user
     * 
     * @param user the user whose tokens should be revoked
     */
    void revokeAllUserTokens(User user);
    
    /**
     * Saves a JWT token for a user
     * 
     * @param user the user associated with the token
     * @param token the JWT token to save
     */
    void saveUserToken(User user, String token);
    
    /**
     * Saves a verification token (for email verification, password reset, etc.)
     * 
     * @param verificationToken the verification token to save
     */
    void saveVerificationToken(VerificationToken verificationToken);
    
    /**
     * Finds a valid one-time password token for a specific user and purpose
     * 
     * @param user the user associated with the token
     * @param otp the one-time password to validate
     * @param tokenType the purpose of the token
     * @return optional containing the token if found and valid
     */
    Optional<VerificationToken> findValidOTP(User user, String otp, TokenPurposeEnum tokenType);
    
    /**
     * Finds a valid token by its value and purpose
     * 
     * @param token the token value to search for
     * @param tokenType the purpose of the token
     * @return optional containing the token if found and valid
     */
    Optional<VerificationToken> findValidToken(String token, TokenPurposeEnum tokenType);
    
    /**
     * Updates an existing verification token
     * 
     * @param token the token to update
     */
    void updateVerificationToken(VerificationToken token);
}
