package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseRequest(
    @NotBlank @Size(max = 50) String code,
    @NotBlank @Size(max = 200) String title
) {
}
