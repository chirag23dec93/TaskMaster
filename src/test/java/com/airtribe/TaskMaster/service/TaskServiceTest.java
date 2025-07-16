package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.entity.*;
import com.airtribe.TaskMaster.exception.BadRequestException;
import com.airtribe.TaskMaster.exception.TaskDeletionException;
import com.airtribe.TaskMaster.repository.TaskRepository;
import com.airtribe.TaskMaster.repository.TaskAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @Mock
    private TaskAssignmentRepository taskAssignmentRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = "user123";
        task = Task.builder()
                .id("task123")
                .title("Test Task")
                .description("Test Description")
                .priority(Priority.MEDIUM)
                .createdBy(userId)
                .build();
    }

    @Test
    void createTask_Success() {
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task createdTask = taskService.create(task);

        assertNotNull(createdTask);
        assertEquals(task.getTitle(), createdTask.getTitle());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_WithPastDueDate_ThrowsException() {
        task.setDueDate(Instant.now().minusSeconds(3600)); // 1 hour in past

        assertThrows(BadRequestException.class, () -> taskService.create(task));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void assignTask_Success() {
        String assigneeId = "assignee123";
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findActiveAssignment(task)).thenReturn(Optional.empty());
        when(userService.findById(assigneeId)).thenReturn(Optional.of(new User()));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task assignedTask = taskService.assignTask(task.getId(), assigneeId, userId);

        assertNotNull(assignedTask);
        verify(taskRepository).save(any(Task.class));
        verify(notificationService).notifyTaskAssignment(eq(assigneeId), any(Task.class), any(TaskAssignment.class));
    }

    @Test
    void assignTask_TaskNotFound_ThrowsException() {
        String assigneeId = "assignee123";
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> taskService.assignTask(task.getId(), assigneeId, userId));
    }

    @Test
    void searchByTitle_Success() {
        List<Task> tasks = Arrays.asList(task);
        when(taskRepository.findByTitleContainingIgnoreCaseAndDeletedFalse("Test")).thenReturn(tasks);

        List<Task> result = taskService.searchByTitle("Test", "createdAt", SortDirection.DESC);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(tasks.size(), result.size());
        assertEquals(tasks.get(0).getId(), result.get(0).getId());
    }

    @Test
    void completeTask_Success() {
        TaskAssignment activeAssignment = new TaskAssignment();
        activeAssignment.setAssignedTo(userId);
        activeAssignment.setStatus(Status.IN_PROGRESS);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findActiveAssignment(task)).thenReturn(Optional.of(activeAssignment));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task completedTask = taskService.completeTask(task.getId(), userId);

        assertNotNull(completedTask);
        verify(taskRepository).save(any(Task.class));
        verify(notificationService).notifyTaskCompletion(eq(userId), any(Task.class), any(TaskArchive.class));
    }

    @Test
    void completeTask_TaskNotFound_ThrowsException() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.completeTask(task.getId(), userId));
    }

    @Test
    void deleteTask_Success() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskAssignmentRepository.findActiveAssignment(task)).thenReturn(Optional.empty());

        taskService.deleteTask(task.getId(), userId);

        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_TaskNotFound_ThrowsException() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.deleteTask(task.getId(), userId));
    }

    @Test
    void deleteTask_NonCreator_ThrowsException() {
        task.setCreatedBy("otherUser");
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThrows(TaskDeletionException.class, () -> taskService.deleteTask(task.getId(), userId));
        verify(taskRepository, never()).save(any(Task.class));
    }
}
