package com.projectmanager.dto.step;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StepCreateDto {

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

}
