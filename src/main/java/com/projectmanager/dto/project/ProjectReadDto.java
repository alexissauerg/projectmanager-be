package com.projectmanager.dto.project;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class ProjectReadDto {

    private UUID id;
    private String name;
    private String description;
    private Set<UUID> userIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
