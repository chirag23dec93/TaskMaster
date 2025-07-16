package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.entity.*;
import com.airtribe.TaskMaster.repository.NotificationRepository;
import com.airtribe.TaskMaster.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    private Task task;
    private String userId;
    private Notification notification;

    @BeforeEach
    void setUp() {
        userId = "user123";
        task = Task.builder()
                .id("task123")
                .title("Test Task")
                .build();
        notification = Notification.builder()
                .id("notif123")
                .userId(userId)
                .taskId(task.getId())
                .type(NotificationType.TASK_ASSIGNED)
                .message("Test notification")
                .isRead(false)
                .build();


    }

    @Test
    void notifyTaskAssignment_Success() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        TaskAssignment assignment = new TaskAssignment();
        assignment.setAssignedBy("assigner123");
        notificationService.notifyTaskAssignment(userId, task, assignment);

        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
            eq(userId),
            eq("/notifications"),
            any(Map.class)
        );
    }

    @Test
    void notifyTaskAssignment_UserNotFound() {
        when(userRepository.existsById(userId)).thenReturn(false);

        notificationService.notifyTaskAssignment(userId, task, new TaskAssignment());

        verify(notificationRepository, never()).save(any(Notification.class));
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    @Test
    void getUnreadNotifications_Success() {
        List<Notification> notifications = Arrays.asList(notification);
        when(notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false))
                .thenReturn(notifications);

        List<Notification> result = notificationService.getUnreadNotifications(userId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(notifications.size(), result.size());
        assertEquals(notifications.get(0).getId(), result.get(0).getId());
    }

    @Test
    void markAsRead_Success() {
        when(notificationRepository.findById(notification.getId()))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(notification);

        notificationService.markAsRead(notification.getId());

        verify(notificationRepository).save(argThat(n -> n.isRead()));
    }

    @Test
    void notifyTaskCompletion_Success() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        TaskArchive archive = new TaskArchive();
        archive.setCompletedBy("completer123");
        archive.setAssignedAt(Instant.now().minusSeconds(3600));
        archive.setCompletedAt(Instant.now());

        notificationService.notifyTaskCompletion(userId, task, archive);

        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
            eq(userId),
            eq("/notifications"),
            any(Map.class)
        );
    }

    @Test
    void notifyTaskCompletion_UserNotFound() {
        when(userRepository.existsById(userId)).thenReturn(false);

        notificationService.notifyTaskCompletion(userId, task, new TaskArchive());

        verify(notificationRepository, never()).save(any(Notification.class));
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    @Test
    void notifyTaskUpdate_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        TaskAssignment assignment = new TaskAssignment();
        assignment.setAssignedTo(userId);
        assignment.setAssignedBy("assigner123");
        assignment.setStatus(Status.IN_PROGRESS);

        notificationService.notifyTaskUpdate(task, assignment);

        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
            eq(userId),
            eq("/notifications"),
            any(Map.class)
        );
    }
}
