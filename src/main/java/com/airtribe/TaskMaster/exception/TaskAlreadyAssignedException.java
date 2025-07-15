package com.airtribe.TaskMaster.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Task is already assigned")
public class TaskAlreadyAssignedException extends RuntimeException {
    public TaskAlreadyAssignedException(String taskId, String assignedTo) {
        super(String.format("Task %s is currently assigned to user %s and must be completed first", taskId, assignedTo));
    }
}
