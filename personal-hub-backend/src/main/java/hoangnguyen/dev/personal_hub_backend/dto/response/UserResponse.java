package hoangnguyen.dev.personal_hub_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    @JsonProperty("user_id")
    private Long userID;

    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    private String email;

    @JsonProperty("bio")
    private String bio;

    @JsonProperty("profile_picture")
    private String profilePic;

    @JsonProperty("auth_type")
    private String authType;

    @JsonProperty("show_online_status")
    private boolean showOnlineStatus;
}
