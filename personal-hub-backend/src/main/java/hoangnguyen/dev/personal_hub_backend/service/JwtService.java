package hoangnguyen.dev.personal_hub_backend.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.function.Function;

/**
 * Service interface for JWT-related operations
 * Handles creation, validation, and parsing of JSON Web Tokens
 */
public interface JwtService {
    /**
     * Extract username from a JWT token
     * 
     * @param jwt the token to extract from
     * @return the username
     */
    String extractUsername(String jwt);
    
    /**
     * Extract a specific claim from a JWT token
     * 
     * @param token the token to extract from
     * @param claimsResolver function to resolve specific claim
     * @return the extracted claim value
     */
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    
    /**
     * Generate a new JWT token for a user
     * 
     * @param userDetails the user details
     * @return the generated JWT token
     */
    String generateToken(UserDetails userDetails);
    
    /**
     * Validate if a token is valid for a specific user
     * 
     * @param token the token to validate
     * @param userDetails the user details
     * @return true if token is valid, false otherwise
     */
    boolean isTokenValid(String token, UserDetails userDetails);
    
    /**
     * Check if a token has expired
     * 
     * @param token the token to check
     * @return true if expired, false otherwise
     */
    boolean isTokenExpired(String token);
    
    /**
     * Extract expiration date from a token
     * 
     * @param token the token to extract from
     * @return the expiration date
     */
    Date extractExpiration(String token);
    
    /**
     * Generate a refresh token for a user
     * 
     * @param userDetails the user details
     * @return the generated refresh token
     */
    String generateRefreshToken(UserDetails userDetails);
}