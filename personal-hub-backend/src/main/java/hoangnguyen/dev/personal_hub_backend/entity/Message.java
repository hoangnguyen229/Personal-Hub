package hoangnguyen.dev.personal_hub_backend.entity;

import hoangnguyen.dev.personal_hub_backend.enums.MessageApprovalStatus;
import hoangnguyen.dev.personal_hub_backend.enums.MessageStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageID;

    // Chat 1-1
    @Column(name = "conversation_key")
    private String conversationKey;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp sentAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatusEnum status = MessageStatusEnum.SENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageApprovalStatus approvalStatus;

    @Column(name = "approved_at")
    private Timestamp approvedAt;


//    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Attachment> attachments;

    // Các trường thời gian
    @PrePersist
    protected void onCreate() {
        sentAt = new Timestamp(System.currentTimeMillis());
    }
}
