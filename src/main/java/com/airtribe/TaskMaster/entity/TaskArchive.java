package com.airtribe.TaskMaster.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "task_archives")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskArchive {
    private static final Logger logger = LoggerFactory.getLogger(TaskArchive.class);
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(name = "completed_by")
    private String completedBy;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "assigned_by")
    private String assignedBy;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "time_taken_minutes")
    private Long timeTakenMinutes;

    @PrePersist
    void calculateTimeTaken() {
        if (assignedAt != null && completedAt != null && task.getDueDate() != null) {
            // Calculate time taken from assignment to completion
            Long actualTime = Duration.between(assignedAt, completedAt).toMinutes();
            
            // Calculate expected time from assignment to due date
            Long expectedTime = Duration.between(assignedAt, task.getDueDate()).toMinutes();
            
            // Store actual time taken
            timeTakenMinutes = actualTime;
            
            // Log if task was completed on time
            if (actualTime <= expectedTime) {
                logger.info("Task {} completed {} minutes before deadline", 
                    task.getId(), expectedTime - actualTime);
            } else {
                logger.info("Task {} completed {} minutes after deadline", 
                    task.getId(), actualTime - expectedTime);
            }
        } else if (assignedAt != null && completedAt != null) {
            // If no due date, just store actual time taken
            timeTakenMinutes = Duration.between(assignedAt, completedAt).toMinutes();
            logger.info("Task {} completed in {} minutes (no due date)", 
                task.getId(), timeTakenMinutes);
        }
    }
}
