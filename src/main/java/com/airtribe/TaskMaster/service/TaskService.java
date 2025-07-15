package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.entity.Status;
import com.airtribe.TaskMaster.entity.Task;
import com.airtribe.TaskMaster.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.Instant;
import java.time.Duration;
import com.airtribe.TaskMaster.entity.*;
import com.airtribe.TaskMaster.repository.*;
import com.airtribe.TaskMaster.exception.TaskAlreadyAssignedException;
import com.airtribe.TaskMaster.exception.TaskDeletionException;
import com.airtribe.TaskMaster.exception.BadRequestException;
import com.airtribe.TaskMaster.entity.SortDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskArchiveRepository taskArchiveRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, 
                     NotificationService notificationService, 
                     UserService userService,
                     TaskAssignmentRepository taskAssignmentRepository,
                     TaskArchiveRepository taskArchiveRepository) {
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
        this.userService = userService;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskArchiveRepository = taskArchiveRepository;
    }

    public Task create(Task task) {
        return taskRepository.save(task);
    }

    public Task update(Task task, TaskAssignment assignment) {
        Task savedTask = taskRepository.save(task);
        notificationService.notifyTaskUpdate(savedTask, assignment);
        return savedTask;
    }

    @Transactional
    public Task completeTask(String taskId, String completedBy) {
        // Find task and active assignment
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        TaskAssignment assignment = taskAssignmentRepository.findFirstByTaskAndStatusOrderByAssignedAtDesc(task, Status.PENDING)
                .orElseThrow(() -> new RuntimeException("No active assignment found for task: " + taskId));

        // Create archive entry
        TaskArchive archive = TaskArchive.builder()
                .task(task)
                .completedBy(completedBy)
                .completedAt(Instant.now())
                .assignedTo(assignment.getAssignedTo())
                .assignedBy(assignment.getAssignedBy())
                .assignedAt(assignment.getAssignedAt())
                .build();

        // Save archive and delete assignment
        taskArchiveRepository.save(archive);
        taskAssignmentRepository.delete(assignment);
        logger.info("Deleted task assignment for completed task: {}", taskId);

        // Calculate time taken
        if (task.getDueDate() != null) {
            Duration timeTaken = Duration.between(assignment.getAssignedAt(), archive.getCompletedAt());
            Duration expectedTime = Duration.between(assignment.getAssignedAt(), task.getDueDate());
            
            if (timeTaken.compareTo(expectedTime) <= 0) {
                logger.info("Task {} completed by {} in {} minutes (before deadline)", 
                    taskId, completedBy, timeTaken.toMinutes());
            } else {
                logger.info("Task {} completed by {} in {} minutes (after deadline)", 
                    taskId, completedBy, timeTaken.toMinutes());
            }
        } else {
            logger.info("Task {} completed by {} (no due date set)", taskId, completedBy);
        }

        return task;
    }

    /**
     * Delete a task and its related records
     * @param taskId The ID of the task to delete
     * @param userId The ID of the user requesting deletion
     * @throws TaskDeletionException if task is currently assigned or user is not authorized
     */
    @Transactional
    public void deleteTask(String taskId, String userId) {
        // Validate input
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new BadRequestException("Task ID cannot be null or empty");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new BadRequestException("User ID cannot be null or empty");
        }

        // Find task
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        // Check if user is authorized (must be task creator)
        if (!task.getCreatedBy().equals(userId)) {
            throw new TaskDeletionException("Only the task creator can delete the task");
        }

        // Check if task has active assignments
        Optional<TaskAssignment> activeAssignment = taskAssignmentRepository.findActiveAssignment(task);
        if (activeAssignment.isPresent()) {
            throw new TaskDeletionException("Cannot delete task while it is assigned to user: " 
                + activeAssignment.get().getAssignedTo());
        }

        // Delete any pending assignments
        taskAssignmentRepository.deleteByTask(task);

        // Mark task as deleted but keep archive records
        task.setDeleted(true);
        task.setDeletedAt(Instant.now());
        task.setDeletedBy(userId);
        taskRepository.save(task);
        
        logger.info("Task {} marked as deleted by user {}", taskId, userId);
    }

    public Task findById(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found: " + id));
    }

    public List<Task> findByUser(String userId) {
        // Find all active assignments for user
        return taskRepository.findAll().stream()
            .filter(task -> taskAssignmentRepository.findFirstByTaskAndStatusOrderByAssignedAtDesc(task, Status.PENDING)
                .map(assignment -> assignment.getAssignedTo().equals(userId))
                .orElse(false))
            .toList();
    }

    public List<Task> findByStatus(Status status) {
        // Find tasks by assignment status
        return taskRepository.findAll().stream()
            .filter(task -> taskAssignmentRepository.findFirstByTaskAndStatusOrderByAssignedAtDesc(task, status)
                .isPresent())
            .toList();
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Transactional
    public Task assignTask(String taskId, String userId, String assignedBy) {
        logger.info("Assigning task {} to user {}", taskId, userId);

        // Verify user exists
        userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Verify task exists
        // Validate input
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new BadRequestException("Task ID cannot be null or empty");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new BadRequestException("User ID cannot be null or empty");
        }
        if (assignedBy == null || assignedBy.trim().isEmpty()) {
            throw new BadRequestException("AssignedBy cannot be null or empty");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        // Check if task has a pending assignment
        Optional<TaskAssignment> currentAssignment = taskAssignmentRepository.findActiveAssignment(task);
        if (currentAssignment.isPresent()) {
            TaskAssignment existing = currentAssignment.get();
            throw new TaskAlreadyAssignedException(taskId, existing.getAssignedTo());
        }

        logger.debug("Current task state: {}", task);

        // Create task assignment
        TaskAssignment assignment = TaskAssignment.builder()
                .task(task)
                .assignedTo(userId)
                .assignedBy(assignedBy)
                .status(Status.PENDING)
                .build();

        assignment = taskAssignmentRepository.save(assignment);
        logger.info("Created task assignment for task {} to user {}", taskId, userId);

        notificationService.notifyTaskAssignment(userId, task, assignment);
        return task;
    }

    /**
     * Search tasks by title with optional sorting
     * @param title The title search term
     * @param sortField Field to sort by (title, description, or createdAt)
     * @param sortDir Sort direction (ASC or DESC)
     * @return List of tasks with matching title
     */
    public List<Task> searchByTitle(String title, String sortField, SortDirection sortDir) {
        String field = sortField != null ? sortField : "createdAt";
        if (sortDir == SortDirection.ASC) {
            return taskRepository.findByTitleContainingIgnoreCaseAndDeletedFalseAsc(title, field);
        } else {
            return taskRepository.findByTitleContainingIgnoreCaseAndDeletedFalseDesc(title, field);
        }
    }

    /**
     * Search tasks by description with optional sorting
     * @param description The description search term
     * @param sortField Field to sort by (title, description, or createdAt)
     * @param sortDir Sort direction (ASC or DESC)
     * @return List of tasks with matching description
     */
    public List<Task> searchByDescription(String description, String sortField, SortDirection sortDir) {
        String field = sortField != null ? sortField : "createdAt";
        if (sortDir == SortDirection.ASC) {
            return taskRepository.findByDescriptionContainingIgnoreCaseAndDeletedFalseAsc(description, field);
        } else {
            return taskRepository.findByDescriptionContainingIgnoreCaseAndDeletedFalseDesc(description, field);
        }
    }

    /**
     * Search tasks that match both title AND description with optional sorting
     * @param title The title search term
     * @param description The description search term
     * @param sortField Field to sort by (title, description, or createdAt)
     * @param sortDir Sort direction (ASC or DESC)
     * @return List of tasks matching both criteria
     */
    public List<Task> searchByTitleAndDescription(String title, String description, String sortField, SortDirection sortDir) {
        String field = sortField != null ? sortField : "createdAt";
        if (sortDir == SortDirection.ASC) {
            return taskRepository.findByTitleAndDescriptionContainingIgnoreCaseAsc(title, description, field);
        } else {
            return taskRepository.findByTitleAndDescriptionContainingIgnoreCaseDesc(title, description, field);
        }
    }

    /**
     * Search tasks that match either title OR description with optional sorting
     * @param title The title search term
     * @param description The description search term
     * @param sortField Field to sort by (title, description, or createdAt)
     * @param sortDir Sort direction (ASC or DESC)
     * @return List of tasks matching either criteria
     */
    public List<Task> searchByTitleOrDescription(String title, String description, String sortField, SortDirection sortDir) {
        String field = sortField != null ? sortField : "createdAt";
        if (sortDir == SortDirection.ASC) {
            return taskRepository.findByTitleOrDescriptionContainingIgnoreCaseAsc(title, description, field);
        } else {
            return taskRepository.findByTitleOrDescriptionContainingIgnoreCaseDesc(title, description, field);
        }
    }

    public Task updateTask(String taskId, Task updatedTask) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // Update fields
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setPriority(updatedTask.getPriority());

        // Save the updated task
        return taskRepository.save(existingTask);
    }
}
