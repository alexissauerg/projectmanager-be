package com.projectmanager.dto.step;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StepReadDto {

    private UUID id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
