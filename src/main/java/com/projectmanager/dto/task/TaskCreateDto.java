package com.projectmanager.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class TaskCreateDto {

    @NotBlank
    @Size(min = 2, max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    private UUID assignedTo;

    private UUID stepId;

}
