package com.airtribe.TaskMaster.controller;

import com.airtribe.TaskMaster.config.TestConfig;
import com.airtribe.TaskMaster.entity.Attachment;
import com.airtribe.TaskMaster.entity.Task;
import com.airtribe.TaskMaster.service.AttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@WebMvcTest(AttachmentController.class)
@Import(TestConfig.class)
@ExtendWith(MockitoExtension.class)
class AttachmentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttachmentService attachmentService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private Attachment attachment;
    private Task task;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        task = Task.builder()
                .id("550e8400-e29b-41d4-a716-446655440000")
                .title("Test Task")
                .build();

        attachment = Attachment.builder()
            .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
            .filename("test.txt")
            .data("test content".getBytes())
            .task(task)
            .build();
    }

    @Test
    @WithMockUser(username = "testuser")
    void uploadAttachment_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "test content".getBytes()
        );

        when(attachmentService.upload(eq(UUID.fromString(task.getId())), any(MultipartFile.class)))
                .thenReturn(attachment);

        mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", task.getId())
                .file(file)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(attachment.getId().toString()))
                .andExpect(jsonPath("$.filename").value("test.txt"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getTaskAttachments_Success() throws Exception {
        List<Attachment> attachments = Arrays.asList(attachment);
        when(attachmentService.list(UUID.fromString(task.getId()))).thenReturn(attachments);

        mockMvc.perform(get("/api/tasks/{taskId}/attachments", task.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(attachment.getId().toString()))
                .andExpect(jsonPath("$[0].filename").value("test.txt"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void downloadAttachment_Success() throws Exception {
        when(attachmentService.get(eq(UUID.fromString(task.getId())), eq(attachment.getId())))
                .thenReturn(attachment);

        mockMvc.perform(get("/api/tasks/{taskId}/attachments/{fileId}", task.getId(), attachment.getId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.txt\""))
                .andExpect(content().bytes(attachment.getData()));
    }

    @Test
    void uploadAttachment_Unauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "test content".getBytes()
        );

        mockMvc.perform(multipart("/api/tasks/{taskId}/attachments", task.getId())
                .file(file)
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
