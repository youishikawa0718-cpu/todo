package com.example.todo.controller.task;

import com.example.todo.service.task.TaskStatus;

import java.util.List;

public record TaskSearchForm(

    String summary,

    List<TaskStatus> status
) {

    public TaskSearchDTO toDTO() {
        return new TaskSearchDTO(summary(),status());
    }
}
