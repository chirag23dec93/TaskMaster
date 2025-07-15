package com.airtribe.TaskMaster.controller;
import com.airtribe.TaskMaster.entity.Status;
import com.airtribe.TaskMaster.entity.Task;
import com.airtribe.TaskMaster.entity.SortDirection;
import com.airtribe.TaskMaster.service.TaskService;
import com.airtribe.TaskMaster.exception.BadRequestException;
import com.airtribe.TaskMaster.exception.TaskAlreadyAssignedException;
import com.airtribe.TaskMaster.exception.TaskAlreadyCompletedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task Management", description = "APIs for managing tasks")
public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(
        summary = "Create a new task",
        description = "Create a new task with the given details"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid task data",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<Task> createTask(
            @Parameter(description = "Task details", required = true)
            @RequestBody Task task,
            
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetails userDetails) {
        task.setCreatedBy(userDetails.getUsername());
        
        // Validate due date
        if (task.getDueDate() != null && task.getDueDate().isBefore(java.time.Instant.now())) {
            throw new BadRequestException("Due date cannot be in the past");
        }
        
        return ResponseEntity.ok(taskService.create(task));
    }
    @Operation(
        summary = "Get a task by ID",
        description = "Retrieve a task's details by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Task> get(
            @Parameter(description = "Task ID", required = true)
            @PathVariable String id) {
        return ResponseEntity.ok(taskService.findById(id));
    }
    /**
     * Search for tasks based on title or description
     * @param title Optional search term for task title
     * @param description Optional search term for task description
     * @param searchType Optional type of search: 'any' (default) or 'both'
     * @return List of matching tasks
     */
    /**
     * Search tasks with sorting options
     * @param title Optional search term for task title
     * @param description Optional search term for task description
     * @param searchType Optional type of search: 'any' (default) or 'both'
     * @param sortField Optional field to sort by: 'title', 'description', or 'createdAt' (default)
     * @param sortDir Optional sort direction: 'ASC' or 'DESC' (default)
     * @return List of matching tasks
     */
    @Operation(
        summary = "Search tasks",
        description = "Search tasks by title and/or description with sorting options"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Tasks found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid search parameters",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content
        )
    })
    @GetMapping("/search")
    public ResponseEntity<List<Task>> searchTasks(
            @Parameter(description = "Search term for task title") 
            @RequestParam(required = false) String title,
            
            @Parameter(description = "Search term for task description")
            @RequestParam(required = false) String description,
            
            @Parameter(description = "Type of search: 'any' (default) or 'both'")
            @RequestParam(required = false, defaultValue = "any") String searchType,
            
            @Parameter(description = "Field to sort by: 'title', 'description', or 'createdAt'")
            @RequestParam(required = false) String sortField,
            
            @Parameter(description = "Sort direction: 'ASC' or 'DESC' (default)")
            @RequestParam(required = false, defaultValue = "DESC") String sortDir) {
        
        logger.info("Searching tasks with title: {}, description: {}, type: {}, sort: {} {}", 
            title, description, searchType, sortField, sortDir);

        // Validate search terms
        if (title == null && description == null) {
            throw new BadRequestException("At least one search term (title or description) is required");
        }

        // Validate sort direction
        SortDirection direction;
        try {
            direction = SortDirection.valueOf(sortDir.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Sort direction must be 'ASC' or 'DESC'");
        }

        // Search based on type
        if ("both".equalsIgnoreCase(searchType)) {
            // Match both title AND description
            if (title == null || description == null) {
                throw new BadRequestException("Both title and description are required for 'both' search type");
            }
            return ResponseEntity.ok(taskService.searchByTitleAndDescription(title, description, sortField, direction));
        } else {
            // Match either title OR description
            if (title != null && description != null) {
                return ResponseEntity.ok(taskService.searchByTitleOrDescription(title, description, sortField, direction));
            } else if (title != null) {
                return ResponseEntity.ok(taskService.searchByTitle(title, sortField, direction));
            } else {
                return ResponseEntity.ok(taskService.searchByDescription(description, sortField, direction));
            }
        }
    }
    @GetMapping("/filter")
    public ResponseEntity<List<Task>> listByStatus(@RequestParam(required=false) String status,
                                           @AuthenticationPrincipal UserDetails user) {
        if (status != null) return ResponseEntity.ok(taskService.findByStatus(Status.valueOf(status)));
        return ResponseEntity.ok(taskService.findByUser(user.getUsername()));
    }
    @PutMapping("/{id}/complete")
    public ResponseEntity<Task> complete(@PathVariable String id, @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.completeTask(id, userDetails.getUsername()));
    }
    @PutMapping("/{id}/assign/{userId}")
    public ResponseEntity<Task> assign(@PathVariable String id, 
                                     @PathVariable String userId,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Received request to assign task {} to user {} by {}", id, userId, userDetails.getUsername());
        
        // Validate input
        if (id == null || id.trim().isEmpty()) {
            throw new BadRequestException("Task ID cannot be null or empty");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new BadRequestException("User ID cannot be null or empty");
        }
        
        try {
            Task assignedTask = taskService.assignTask(id, userId, userDetails.getUsername());
            logger.info("Successfully assigned task {} to user {} by {}", id, userId, userDetails.getUsername());
            return ResponseEntity.ok(assignedTask);
        } catch (TaskAlreadyAssignedException | TaskAlreadyCompletedException e) {
            // Log the business logic exception but preserve the original exception type
            logger.info("Business validation failed for task assignment: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // Log unexpected errors
            logger.error("Failed to assign task {} to user {}: {}", id, userId, e.getMessage(), e);
            throw new RuntimeException("Failed to assign task: " + e.getMessage(), e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable String id,
            @RequestBody Task updatedTask) {
        return ResponseEntity.ok(taskService.updateTask(id, updatedTask));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Received request to delete task {} by {}", id, userDetails.getUsername());
        taskService.deleteTask(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
