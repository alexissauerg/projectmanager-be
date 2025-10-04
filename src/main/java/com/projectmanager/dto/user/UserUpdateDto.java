package com.projectmanager.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDto {

    @Size(min = 2, max = 100)
    private String name;

}
