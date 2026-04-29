package com.example.skhubox.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordResetRequest {

    @NotBlank(message = "학번은 필수 입력 사항입니다.")
    @Pattern(regexp = "^[0-9]{9}$", message = "학번은 9자리로 입력해주세요.")
    private String studentNumber;

    @NotBlank(message = "이메일은 필수 입력 사항입니다.")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@office\\.skhu\\.ac\\.kr$", message = "올바른 이메일 형식이 아닙니다.")
    private String email;
}
