package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.entity.Attachment;
import com.airtribe.TaskMaster.entity.Task;
import com.airtribe.TaskMaster.repository.AttachmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository repo;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private AttachmentService service;

    private Task task;
    private Attachment attachment;
    private MultipartFile file;

    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id("550e8400-e29b-41d4-a716-446655440000")
                .title("Test Task")
                .build();

        file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "test content".getBytes()
        );

        attachment = Attachment.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .filename("test.txt")
                .data("test content".getBytes())
                .task(task)
                .build();
    }

    @Test
    void upload_Success() throws IOException {
        when(taskService.findById(task.getId())).thenReturn(task);
        when(repo.save(any(Attachment.class))).thenReturn(attachment);

        Attachment result = service.upload(UUID.fromString(task.getId()), file);

        assertNotNull(result);
        assertEquals(attachment.getId(), result.getId());
        assertEquals(attachment.getFilename(), result.getFilename());
        assertArrayEquals(attachment.getData(), result.getData());
        assertEquals(attachment.getTask().getId(), result.getTask().getId());
    }

    @Test
    void list_Success() {
        List<Attachment> attachments = Arrays.asList(attachment);
        when(repo.findByTaskId(task.getId())).thenReturn(attachments);

        List<Attachment> result = service.list(UUID.fromString(task.getId()));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(attachment.getId(), result.get(0).getId());
        assertEquals(attachment.getFilename(), result.get(0).getFilename());
    }

    @Test
    void get_Success() {
        List<Attachment> attachments = Arrays.asList(attachment);
        when(repo.findByTaskId(task.getId())).thenReturn(attachments);

        Attachment result = service.get(UUID.fromString(task.getId()), attachment.getId());

        assertNotNull(result);
        assertEquals(attachment.getId(), result.getId());
        assertEquals(attachment.getFilename(), result.getFilename());
        assertArrayEquals(attachment.getData(), result.getData());
        assertEquals(attachment.getTask().getId(), result.getTask().getId());
    }
}
