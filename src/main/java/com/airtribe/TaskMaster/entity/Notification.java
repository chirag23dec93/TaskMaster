package com.airtribe.TaskMaster.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Notification {
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String userId;
    private String message;
    private String taskId;
    
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    @Column(name = "is_read")
    private boolean isRead;
    private Instant createdAt;
    
    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
