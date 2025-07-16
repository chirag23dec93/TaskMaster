package com.airtribe.TaskMaster.controller;

import com.airtribe.TaskMaster.config.TestConfig;
import com.airtribe.TaskMaster.entity.Priority;
import com.airtribe.TaskMaster.entity.Task;
import com.airtribe.TaskMaster.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(TestConfig.class)
class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

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

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "user123")
    void createTask_Success() throws Exception {
        when(taskService.create(any(Task.class))).thenReturn(task);

        mockMvc.perform(post("/api/tasks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(task.getTitle()))
                .andExpect(jsonPath("$.description").value(task.getDescription()));

        verify(taskService).create(any(Task.class));
    }

    @Test
    @WithMockUser(username = "user123")
    void assignTask_Success() throws Exception {
        String assigneeId = "assignee123";
        when(taskService.assignTask(eq(task.getId()), eq(assigneeId), any())).thenReturn(task);

        mockMvc.perform(put("/api/tasks/{id}/assign/{userId}", task.getId(), assigneeId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()));

        verify(taskService).assignTask(eq(task.getId()), eq(assigneeId), any());
    }

    @Test
    @WithMockUser(username = "user123")
    void searchTasks_Success() throws Exception {
        List<Task> tasks = Arrays.asList(task);
        when(taskService.searchByTitle(eq("Test"), eq("createdAt"), any()))
                .thenReturn(tasks);

        mockMvc.perform(get("/api/tasks/search")
                .param("title", "Test")
                .param("sortField", "createdAt")
                .param("sortDir", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(task.getTitle()));

        verify(taskService).searchByTitle(eq("Test"), eq("createdAt"), any());
    }

    @Test
    @WithMockUser(username = "user123")
    void completeTask_Success() throws Exception {
        when(taskService.completeTask(eq(task.getId()), any()))
                .thenReturn(task);

        mockMvc.perform(put("/api/tasks/{id}/complete", task.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()));

        verify(taskService).completeTask(eq(task.getId()), any());
    }

    @Test
    @WithMockUser(username = "user123")
    void deleteTask_Success() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", task.getId())
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(eq(task.getId()), any());
    }

    @Test
    void unauthorizedAccess_Failure() throws Exception {
        mockMvc.perform(post("/api/tasks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task)))
                .andExpect(status().isUnauthorized());
    }
}
