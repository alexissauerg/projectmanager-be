package com.projectmanager.dto.project;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectUpdateDto {

    @Size(min = 2, max = 100)
    private String name;

    @Size(max = 500)
    private String description;

}
