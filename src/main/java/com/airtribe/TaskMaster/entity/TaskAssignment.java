package com.airtribe.TaskMaster.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "task_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "assigned_by")
    private String assignedBy;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    @PrePersist
    void prePersist() {
        assignedAt = Instant.now();
    }
}
