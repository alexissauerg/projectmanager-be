package com.projectmanager.dto.user;

import com.projectmanager.entity.Role;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserReadDto {

    private UUID id;
    private String email;
    private String name;
    private Role role;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
