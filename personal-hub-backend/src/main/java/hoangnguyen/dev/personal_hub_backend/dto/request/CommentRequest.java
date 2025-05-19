package hoangnguyen.dev.personal_hub_backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequest {
    @NotEmpty(message = "Comment content cannot be empty")
    private String content;
    
    @NotNull(message = "Post ID cannot be null")
    private Long postId;
}
