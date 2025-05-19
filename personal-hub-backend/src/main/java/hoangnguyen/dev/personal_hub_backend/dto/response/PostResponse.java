package hoangnguyen.dev.personal_hub_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    @JsonProperty("post_id")
    private Long postID;

    @JsonProperty("title")
    private String title;

    @JsonProperty("content")
    private String content;

    @JsonProperty("slug")
    private String slug;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("updated_at")
    private Timestamp updatedAt;

    @JsonProperty("deleted_at")
    private Timestamp deletedAt;

    @JsonProperty("user")
    private UserResponse user;

    @JsonProperty("category")
    private CategoryResponse category;

    @JsonProperty("images")
    private List<ImageResponse> images;

    @JsonProperty("tags")
    private Set<TagResponse> tags;

    @JsonProperty("comments")
    private List<CommentResponse> comments;

    @JsonProperty("likes")
    private Set<LikeResponse> likes;
}
