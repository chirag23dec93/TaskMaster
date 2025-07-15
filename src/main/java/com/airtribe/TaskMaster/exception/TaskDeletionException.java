package com.airtribe.TaskMaster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Task cannot be deleted")
public class TaskDeletionException extends RuntimeException {
    public TaskDeletionException(String message) {
        super(message);
    }
}
