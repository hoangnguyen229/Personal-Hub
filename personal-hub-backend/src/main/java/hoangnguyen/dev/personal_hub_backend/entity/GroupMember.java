package hoangnguyen.dev.personal_hub_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "group_members")
public class GroupMember {
    @Id
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp joinedAt;

    @PrePersist
    protected void onCreate() {
        joinedAt = new Timestamp(System.currentTimeMillis());
    }
}
