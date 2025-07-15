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
public class CommentService {
    private final CommentRepository comments;
    private final UserService users;
    private final TaskService tasks;

    @Transactional
    public Comment add(UUID taskId, String username, String text) {
        Task task = tasks.findById(taskId.toString());
        User author = (User) users.loadUserByUsername(username);
        Comment c = Comment.builder()
                .text(text)
                .createdAt(Instant.now())
                .author(author)
                .task(task)
                .build();
        return comments.save(c);
    }

    @Transactional(readOnly = true)
    public List<Comment> list(UUID taskId) {
        return comments.findByTaskId(taskId.toString());
    }
}
