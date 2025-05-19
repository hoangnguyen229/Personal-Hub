package hoangnguyen.dev.personal_hub_backend.service.impl;

import hoangnguyen.dev.personal_hub_backend.dto.request.UserRequest;
import hoangnguyen.dev.personal_hub_backend.dto.response.UserResponse;
import hoangnguyen.dev.personal_hub_backend.entity.User;
import hoangnguyen.dev.personal_hub_backend.enums.ErrorCodeEnum;
import hoangnguyen.dev.personal_hub_backend.exception.ApiException;
import hoangnguyen.dev.personal_hub_backend.repository.UserRepository;
import hoangnguyen.dev.personal_hub_backend.service.ImageService;
import hoangnguyen.dev.personal_hub_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ImageService imageService;

    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );
        return mapToUserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );
        return mapToUserResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    @Override
    public UserResponse updateUser(Long userId, UserRequest userRequest) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND)
        );

        user.setUsername(userRequest.getUsername());
        user.setBio(userRequest.getBio());

        if (userRequest.getProfilePic() != null && !userRequest.getProfilePic().isEmpty()) {
            String imageUrl = imageService.uploadImage(userRequest.getProfilePic());
            processProfilePicture(user, imageUrl);
        }

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    private void processProfilePicture(User user, String profilePicUrl) {
        if (imageService.processImageFromTemp(profilePicUrl)) {
            user.setProfilePic(profilePicUrl);
        } else {
            throw new ApiException(ErrorCodeEnum.IMAGE_PROCESSING_FAILED);
        }
    }

    private UserResponse mapToUserResponse(User user){
        return UserResponse.builder()
                .userID(user.getUserID())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .profilePic(user.getProfilePic())
                .authType(user.getAuthType().getValue())
                .build();
    }
}
