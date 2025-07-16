package com.airtribe.TaskMaster.controller;

import com.airtribe.TaskMaster.config.TestConfig;
import com.airtribe.TaskMaster.entity.Notification;
import com.airtribe.TaskMaster.entity.NotificationType;
import com.airtribe.TaskMaster.service.NotificationService;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@WebMvcTest(NotificationController.class)
@Import(TestConfig.class)
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private Notification notification;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        notification = Notification.builder()
                .id("notif123")
                .userId("user123")
                .taskId("task123")
                .type(NotificationType.TASK_ASSIGNED)
                .message("Test notification")
                .isRead(false)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUnreadNotifications_Success() throws Exception {
        List<Notification> notifications = Arrays.asList(notification);
        when(notificationService.getUnreadNotifications(anyString())).thenReturn(notifications);

        mockMvc.perform(get("/api/notifications/unread")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("notif123"))
                .andExpect(jsonPath("$[0].message").value("Test notification"))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getAllNotifications_Success() throws Exception {
        List<Notification> notifications = Arrays.asList(notification);
        when(notificationService.getUserNotifications(anyString())).thenReturn(notifications);

        mockMvc.perform(get("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("notif123"))
                .andExpect(jsonPath("$[0].message").value("Test notification"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void markAsRead_Success() throws Exception {
        mockMvc.perform(post("/api/notifications/{notificationId}/read", "notif123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getUnreadNotifications_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/notifications/unread")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
