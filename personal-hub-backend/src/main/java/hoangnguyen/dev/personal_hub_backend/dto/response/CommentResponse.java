package hoangnguyen.dev.personal_hub_backend.dto.response;

import java.sql.Time;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    @JsonProperty("comment_id")
    private Long commentID;

    @JsonProperty("content")
    private String content;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("deleted_at")
    private Timestamp deletedAt;

    @JsonProperty("user")
    private UserResponse user;
}
