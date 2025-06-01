package hoangnguyen.dev.personal_hub_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfflineMessageResponse {
    private Long senderId;
    private Long receiverId;
    private Long messageId;
    private String content;
    private Timestamp sentAt;
    private String status;
    private String approvalStatus;
}
