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
public class MessageResponse {
    @JsonProperty("message_id")
    private Long messageID;

    @JsonProperty("sender")
    private UserResponse sender;

    @JsonProperty("receiver")
    private UserResponse receiver;

    @JsonProperty("content")
    private String content;

    @JsonProperty("sent_at")
    private Timestamp sentAt;

    @JsonProperty("status")
    private String status;

    @JsonProperty("approval_status")
    private String approvalStatus;

    @JsonProperty("approved_at")
    private Timestamp approvedAt;
}
