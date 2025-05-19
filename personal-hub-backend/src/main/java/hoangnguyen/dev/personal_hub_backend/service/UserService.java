package hoangnguyen.dev.personal_hub_backend.service;

import hoangnguyen.dev.personal_hub_backend.dto.request.UserRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    /**
     * Get user by ID
     * @param userId the ID of the user to retrieve
     * @return the user response DTO
     */
    UserResponse getUserById(Long userId);
    
    /**
     * Get user by email
     * @param email the email of the user to retrieve
     * @return the user response DTO
     */
    UserResponse getUserByEmail(String email);
    
    /**
     * Get all users
     * @return list of all user response DTOs
     */
    List<UserResponse> getAllUsers();
    
    /**
     * Update user information
     * @param userId the ID of the user to update
     * @param userRequest the user request DTO with updated information
     * @return the updated user response DTO
     */
    UserResponse updateUser(Long userId, UserRequest userRequest);
}
