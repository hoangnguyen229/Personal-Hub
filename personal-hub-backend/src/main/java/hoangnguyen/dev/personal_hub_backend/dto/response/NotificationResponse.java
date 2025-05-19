package hoangnguyen.dev.personal_hub_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class NotificationResponse {
    @JsonProperty("notification_id")
    private Long notificationID;

    @JsonProperty("content")
    private String content;

    @JsonProperty("is_read")
    private Boolean isRead = false;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("deleted_at")
    private Timestamp deletedAt;

    @JsonProperty("user")
    private UserResponse user;
}
