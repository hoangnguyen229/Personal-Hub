package hoangnguyen.dev.personal_hub_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FollowResponse {
    @JsonProperty("follow_id")
    private Long followID;

    @JsonProperty("follower_user")
    private UserResponse followerUser;

    @JsonProperty("following_user")
    private UserResponse followingUser;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("updated_at")
    private Timestamp updatedAt;

    @JsonProperty("deleted_at")
    private Timestamp deletedAt;
}
