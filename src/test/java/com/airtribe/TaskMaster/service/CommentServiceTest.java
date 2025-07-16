package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.entity.Comment;
import com.airtribe.TaskMaster.entity.Task;
import com.airtribe.TaskMaster.entity.User;
import com.airtribe.TaskMaster.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private CommentService service;

    private Task task;
    private User user;
    private Comment comment;

    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id("550e8400-e29b-41d4-a716-446655440000")
                .title("Test Task")
                .build();

        user = User.builder()
                .id("550e8400-e29b-41d4-a716-446655440001")
                .username("testuser")
                .email("test@example.com")
                .build();

        comment = Comment.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"))
                .text("Test comment")
                .task(task)
                .author(user)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void add_Success() {
        when(taskService.findById(task.getId())).thenReturn(task);
        when(userService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = service.add(UUID.fromString(task.getId()), user.getUsername(), "Test comment");

        assertNotNull(result);
        assertEquals(comment.getId(), result.getId());
        assertEquals(comment.getText(), result.getText());
        assertEquals(comment.getTask().getId(), result.getTask().getId());
        assertEquals(comment.getAuthor().getUsername(), result.getAuthor().getUsername());
    }

    @Test
    void list_Success() {
        List<Comment> comments = Arrays.asList(comment);
        when(commentRepository.findByTaskId(task.getId())).thenReturn(comments);

        List<Comment> result = service.list(UUID.fromString(task.getId()));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(comment.getId(), result.get(0).getId());
        assertEquals(comment.getText(), result.get(0).getText());
        assertEquals(comment.getTask().getId(), result.get(0).getTask().getId());
        assertEquals(comment.getAuthor().getUsername(), result.get(0).getAuthor().getUsername());
    }
}
