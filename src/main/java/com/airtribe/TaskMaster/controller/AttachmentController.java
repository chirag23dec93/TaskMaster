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
@RequestMapping("/api/tasks/{taskId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {
    private final AttachmentService svc;

    @PostMapping
    public Attachment upload(
            @PathVariable UUID taskId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return svc.upload(taskId, file);
    }

    @GetMapping
    public List<Attachment> list(@PathVariable UUID taskId) {
        return svc.list(taskId);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> download(
            @PathVariable UUID taskId,
            @PathVariable UUID fileId
    ) {
        Attachment a = svc.get(taskId, fileId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + a.getFilename() + "\"")
                .body(a.getData());
    }
}