package com.example.todo.controller.task;

import com.example.todo.service.task.TaskStatus;

import java.util.List;
import java.util.Optional;

public record TaskSearchDTO (

        String summary,

        List<TaskStatus> statusList
){

    public boolean isChecked(String status) {
        return Optional.ofNullable(statusList)
                .map(list -> list.stream()
                        .anyMatch(taskStatus -> taskStatus.name().equals(status)))
                .orElse(false);
    }
}
