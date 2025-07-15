package com.airtribe.TaskMaster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Task is already completed")
public class TaskAlreadyCompletedException extends RuntimeException {
    public TaskAlreadyCompletedException(String taskId) {
        super(String.format("Task %s has already been completed and cannot be reassigned", taskId));
    }
}
