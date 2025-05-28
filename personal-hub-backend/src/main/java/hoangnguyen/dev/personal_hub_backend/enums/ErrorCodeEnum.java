package hoangnguyen.dev.personal_hub_backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCodeEnum {
    OK(200,"Success", HttpStatus.OK),
    EMAIL_ALREADY_EXISTS(409, "Email is already in use by another account", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(401, "Invalid login information", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(404, "Account not registered", HttpStatus.NOT_FOUND),
    REGISTRATION_FAILED(400, "Registration failed", HttpStatus.BAD_REQUEST),
    TOKEN_EXPIRED(401, "Token has expired", HttpStatus.UNAUTHORIZED),
    INVALID_EMAIL_FORMAT(400, "Invalid email format", HttpStatus.BAD_REQUEST),
    WEAK_PASSWORD(400, "Password is not strong enough", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(400, "Invalid username", HttpStatus.BAD_REQUEST),
    TOO_MANY_ATTEMPTS(429, "Too many attempts, please try again later", HttpStatus.TOO_MANY_REQUESTS),
    INVALID_TOKEN(401, "Invalid Token", HttpStatus.UNAUTHORIZED),
    TOKEN_BLACKLISTED(401, "Token has been disabled", HttpStatus.UNAUTHORIZED),
    EMAIL_SENDING_FAILED(409, "Email sending failed", HttpStatus.CONFLICT),
    INVALID_OTP(400, "Invalid OTP", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN_RESET_PASSWORD(400, "Invalid token reset password", HttpStatus.BAD_REQUEST),
    PASSWORD_DO_NOT_MATCH(400, "Password does not match", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND(404, "Category not found", HttpStatus.NOT_FOUND),
    TAG_NOT_FOUND(404,"Tag not found" ,HttpStatus.NOT_FOUND ),
    TOO_MANY_TAGS(400, "Too many tags", HttpStatus.BAD_REQUEST),
    IMAGE_UPLOAD_FAILED(400, "Image upload failed", HttpStatus.BAD_REQUEST),
    IMAGE_DELETE_FAILED(400, "Image delete failed", HttpStatus.BAD_REQUEST),
    EMPTY_FILE(400, "File is empty", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(400, "File size exceeds the limit", HttpStatus.BAD_REQUEST),
    INVALID_FILE_FORMAT(400, "Invalid file format", HttpStatus.BAD_REQUEST),
    IMAGE_NOT_FOUND(404, "Image not found", HttpStatus.NOT_FOUND),
    IMAGE_FETCH_FAILED(500, "Failed to fetch image", HttpStatus.INTERNAL_SERVER_ERROR),
    POST_NOT_FOUND(404, "Post not found", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS(409, "Category already exists", HttpStatus.CONFLICT),
    POST_ALREADY_EXISTS(409, "Post title already exists", HttpStatus.CONFLICT),
    MAX_DUPLICATE_COMMENTS(400, "You have already posted the same comment 3 times on this post. Please write a different comment.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_OPERATION(403, "You are not authorized to perform this operation", HttpStatus.FORBIDDEN),
    COMMENT_NOT_FOUND(404, "Comment not found", HttpStatus.NOT_FOUND),
    IMAGE_PROCESSING_FAILED(400, "Image processing failed", HttpStatus.BAD_REQUEST),
    ALREADY_LIKED(409, "You have already liked this post", HttpStatus.CONFLICT),
    LIKE_NOT_FOUND(404, "Like not found", HttpStatus.NOT_FOUND),
    POST_ALREADY_DELETED(409, "Post has been deleted", HttpStatus.CONFLICT),
    NOTIFICATION_NOT_FOUND(404, "Notification not found", HttpStatus.NOT_FOUND),
    FOLLOW_NOT_FOUND(404, "Follow not found", HttpStatus.NOT_FOUND),
    ALREADY_FOLLOWED(409, "You are already followed this user", HttpStatus.CONFLICT),
    INVALID_NOTIFICATION_TYPE(400, "Invalid notification type", HttpStatus.BAD_REQUEST),
    CANNOT_FOLLOW_YOURSELF(400, "You cannot follow yourself", HttpStatus.BAD_REQUEST),
    SEARCH_FAILED(500, "Search failed", HttpStatus.INTERNAL_SERVER_ERROR),
    MESSAGE_NOT_FOUND(404, "Message not found", HttpStatus.NOT_FOUND),
    INVALID_USER_ID(400, "Invalid user ID", HttpStatus.BAD_REQUEST),
    NO_PENDING_MESSAGES(404, "No pending messages", HttpStatus.NOT_FOUND),
    ;

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}