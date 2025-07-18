package com.airtribe.TaskMaster.repository;

import com.airtribe.TaskMaster.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByTaskId(String taskId);
}
