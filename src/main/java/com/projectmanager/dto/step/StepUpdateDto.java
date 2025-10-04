package com.projectmanager.dto.step;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StepUpdateDto {

    @Size(min = 2, max = 100)
    private String name;

}
