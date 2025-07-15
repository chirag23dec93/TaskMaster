package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.entity.*;
import java.time.Duration;
import com.airtribe.TaskMaster.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.airtribe.TaskMaster.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;



@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate,
                             NotificationRepository notificationRepository,
                             UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void notifyTaskAssignment(String userId, Task task, TaskAssignment assignment) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            logger.error("User not found for ID: {}", userId);
            return;
        }
        logger.info("Creating task assignment notification for user: {} and task: {}", userId, task.getId());
        
        Notification notification = Notification.builder()
                .userId(userId)
                .taskId(task.getId())
                .message(String.format("Task '%s' assigned by %s", task.getTitle(), assignment.getAssignedBy()))
                .type(NotificationType.TASK_ASSIGNED)
                .isRead(false)
                .build();
        
        notification = notificationRepository.save(notification);
        logger.info("Saved notification with ID: {}", notification.getId());
        
        try {
            // Create message object with all necessary fields
            Map<String, Object> messageObj = new HashMap<>();
            messageObj.put("type", notification.getType().toString());
            messageObj.put("message", notification.getMessage());
            messageObj.put("userId", notification.getUserId());
            messageObj.put("taskId", notification.getTaskId());
            messageObj.put("taskTitle", task.getTitle());
            messageObj.put("assignedBy", assignment.getAssignedBy());
            messageObj.put("dueDate", task.getDueDate());
            messageObj.put("taskPriority", task.getPriority().toString());
            messageObj.put("timestamp", notification.getCreatedAt().toEpochMilli());
            messageObj.put("notificationId", notification.getId());
            messageObj.put("read", notification.isRead());
            
            // Send to user-specific topic
            String destination = "/user/" + userId + "/notifications";
            messagingTemplate.convertAndSend(destination, messageObj);
            logger.info("Successfully sent notification to destination: {}", destination);
        } catch (Exception e) {
            logger.error("Failed to send notification to user: {}", userId, e);
        }
    }

    public void notifyTaskCompletion(String userId, Task task, TaskArchive archive) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            logger.error("User not found for ID: {}", userId);
            return;
        }
        logger.info("Creating task completion notification for user: {} and task: {}", userId, task.getId());
        
        String message;
        if (task.getDueDate() != null) {
            Duration timeTaken = Duration.between(archive.getAssignedAt(), archive.getCompletedAt());
            Duration expectedTime = Duration.between(archive.getAssignedAt(), task.getDueDate());
            
            if (timeTaken.compareTo(expectedTime) <= 0) {
                message = String.format("Task '%s' completed by %s in %d minutes (before deadline)", 
                    task.getTitle(), archive.getCompletedBy(), timeTaken.toMinutes());
            } else {
                message = String.format("Task '%s' completed by %s in %d minutes (after deadline)", 
                    task.getTitle(), archive.getCompletedBy(), timeTaken.toMinutes());
            }
        } else {
            message = String.format("Task '%s' completed by %s in %d minutes", 
                task.getTitle(), archive.getCompletedBy(), 
                Duration.between(archive.getAssignedAt(), archive.getCompletedAt()).toMinutes());
        }

        Notification notification = Notification.builder()
                .userId(userId)
                .taskId(task.getId())
                .message(message)
                .type(NotificationType.TASK_COMPLETED)
                .isRead(false)
                .build();
        
        notification = notificationRepository.save(notification);
        logger.info("Saved notification with ID: {}", notification.getId());
        
        try {
            // Create message object with all necessary fields
            Map<String, Object> messageObj = new HashMap<>();
            messageObj.put("type", notification.getType().toString());
            messageObj.put("message", notification.getMessage());
            messageObj.put("userId", notification.getUserId());
            messageObj.put("taskId", notification.getTaskId());
            messageObj.put("taskTitle", task.getTitle());
            messageObj.put("completedBy", archive.getCompletedBy());
            messageObj.put("completedAt", archive.getCompletedAt());
            messageObj.put("assignedAt", archive.getAssignedAt());
            messageObj.put("dueDate", task.getDueDate());
            messageObj.put("taskPriority", task.getPriority().toString());
            messageObj.put("timestamp", notification.getCreatedAt().toEpochMilli());
            messageObj.put("notificationId", notification.getId());
            messageObj.put("read", notification.isRead());
            
            // Send to user-specific topic
            String destination = "/user/" + userId + "/notifications";
            messagingTemplate.convertAndSend(destination, messageObj);
            logger.info("Successfully sent notification to destination: {}", destination);
        } catch (Exception e) {
            logger.error("Failed to send notification to user: {}", userId, e);
        }
    }

    public void notifyTaskUpdate(Task task, TaskAssignment assignment) {
        String userId = assignment.getAssignedTo();
        logger.info("Creating task update notification for user: {} and task: {}", userId, task.getId());
        
        // Create a more descriptive message
        String message = String.format("Task '%s' has been updated - Status: %s, Priority: %s",
            task.getTitle(),
            assignment.getStatus(),
            task.getPriority());
        
        Notification notification = Notification.builder()
                .userId(userId)
                .taskId(task.getId())
                .message(message)
                .type(NotificationType.TASK_UPDATED)
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);
        logger.info("Saved update notification with ID: {}", notification.getId());

        try {
            // Create message object with all necessary fields
            Map<String, Object> messageObj = new HashMap<>();
            messageObj.put("type", notification.getType().toString());
            messageObj.put("message", notification.getMessage());
            messageObj.put("userId", notification.getUserId());
            messageObj.put("taskId", notification.getTaskId());
            messageObj.put("taskTitle", task.getTitle());
            messageObj.put("taskPriority", task.getPriority().toString());
            messageObj.put("assignedTo", assignment.getAssignedTo());
            messageObj.put("assignedBy", assignment.getAssignedBy());
            messageObj.put("dueDate", task.getDueDate());
            messageObj.put("status", assignment.getStatus().toString());
            messageObj.put("timestamp", notification.getCreatedAt().toEpochMilli());
            messageObj.put("notificationId", notification.getId());
            messageObj.put("read", notification.isRead());
            
            // Send to user-specific topic
            String destination = "/user/" + userId + "/notifications";
            messagingTemplate.convertAndSend(destination, messageObj);
            logger.info("Successfully sent update notification to destination: {}", destination);
        } catch (Exception e) {
            logger.error("Failed to send update notification to user: {}", userId, e);
        }
    }

    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false);
    }

    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }


}
