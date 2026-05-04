package com.example.skhubox.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "이름은 필수 입력 사항입니다.")
    private String name;

    private String department;
}
