package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.entity.Team;
import com.airtribe.TaskMaster.entity.Comment;
import com.airtribe.TaskMaster.entity.Attachment;
import com.airtribe.TaskMaster.entity.User;
import com.airtribe.TaskMaster.entity.Task;
import com.airtribe.TaskMaster.repository.TeamRepository;
import com.airtribe.TaskMaster.repository.CommentRepository;
import com.airtribe.TaskMaster.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {
    private final AttachmentRepository repo;
    private final TaskService tasks;

    @Transactional
    public Attachment upload(UUID taskId, MultipartFile file) throws IOException {
        Task task = tasks.findById(taskId.toString());
        Attachment a = Attachment.builder()
                .filename(file.getOriginalFilename())
                .data(file.getBytes())
                .task(task)
                .build();
        return repo.save(a);
    }

    @Transactional(readOnly = true)
    public List<Attachment> list(UUID taskId) {
        return repo.findByTaskId(taskId.toString());
    }

    @Transactional(readOnly = true)
    public Attachment get(UUID taskId, UUID fileId) {
        try {
            List<Attachment> a = repo.findByTaskId(taskId.toString());
           // System.out.println
//            if (!a.getTask().getId().equals(taskId)) {
//                throw new IllegalArgumentException("Attachment does not belong to task");
//            }
            return a.get(0);
        } catch (Exception e) {
            System.out.println("Attachment not found: " + fileId);

        }
        return null;
    }
}