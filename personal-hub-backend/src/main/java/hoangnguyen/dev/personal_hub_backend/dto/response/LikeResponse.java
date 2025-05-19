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
public class LikeResponse {
    @JsonProperty("like_id")
    private Long likeID;

    @JsonProperty("post_id")
    private Long postID;

    @JsonProperty("user_id")
    private Long userID;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("deleted_at")
    private Timestamp deletedAt;
}
