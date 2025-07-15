package com.airtribe.TaskMaster.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "task")  // Explicitly specify the table name
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    private Priority priority;
    
    private Instant dueDate;
    
    private String createdBy;
    
    private Instant createdAt;
    private Instant updatedAt;
    @Builder.Default
    private boolean deleted = false;
    private Instant deletedAt;
    private String deletedBy;
    @PrePersist void prePersist() { createdAt = Instant.now(); updatedAt = Instant.now(); }
    @PreUpdate void preUpdate() { updatedAt = Instant.now(); }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}