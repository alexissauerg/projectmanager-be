package com.projectmanager.dto.task;

import com.projectmanager.entity.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaskReadDto {

    private UUID id;
    private String title;
    private String description;
    private UUID assignedToId;
    private UUID stepId;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
