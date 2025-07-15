package com.airtribe.TaskMaster.controller;

import com.airtribe.TaskMaster.entity.Team;
import com.airtribe.TaskMaster.entity.Comment;
import com.airtribe.TaskMaster.entity.Attachment;
import com.airtribe.TaskMaster.service.TeamService;
import com.airtribe.TaskMaster.service.CommentService;
import com.airtribe.TaskMaster.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService svc;

    @PostMapping
    public Comment add(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UserDetails user,
            @RequestBody Comment dto
    ) {
        return svc.add(taskId, user.getUsername(), dto.getText());
    }

    @GetMapping
    public List<Comment> list(@PathVariable UUID taskId) {
        return svc.list(taskId);
    }
}
