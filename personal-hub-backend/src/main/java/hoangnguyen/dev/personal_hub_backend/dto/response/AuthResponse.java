package hoangnguyen.dev.personal_hub_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    @JsonProperty("access_token")
    private String access_token;

    @JsonProperty("message")
    private String message;

    @JsonProperty("user")
    private UserResponse user;
}
