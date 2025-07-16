package com.airtribe.TaskMaster.controller;

import com.airtribe.TaskMaster.config.TestConfig;
import com.airtribe.TaskMaster.entity.Comment;
import com.airtribe.TaskMaster.entity.Task;
import com.airtribe.TaskMaster.entity.User;
import com.airtribe.TaskMaster.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@WebMvcTest(CommentController.class)
@Import(TestConfig.class)
@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CommentService commentService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private Comment comment;
    private Task task;
    private User user;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        user = new User();
        user.setId("user123");
        user.setUsername("testuser");

        task = Task.builder()
                .id("550e8400-e29b-41d4-a716-446655440000")
                .title("Test Task")
                .build();

        comment = Comment.builder()
            .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
            .text("Test comment")
            .task(task)
            .author(user)
            .createdAt(Instant.now())
            .build();
    }

    @Test
    @WithMockUser(username = "testuser")
    void addComment_Success() throws Exception {
        when(commentService.add(eq(UUID.fromString(task.getId())), eq("testuser"), eq("Test comment")))
                .thenReturn(comment);

        mockMvc.perform(post("/api/tasks/{taskId}/comments", task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId().toString()))
                .andExpect(jsonPath("$.text").value("Test comment"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getTaskComments_Success() throws Exception {
        List<Comment> comments = Arrays.asList(comment);
        when(commentService.list(UUID.fromString(task.getId()))).thenReturn(comments);

        mockMvc.perform(get("/api/tasks/{taskId}/comments", task.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(comment.getId().toString()))
                .andExpect(jsonPath("$[0].text").value("Test comment"));
    }

    @Test
    void addComment_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/tasks/{taskId}/comments", task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
