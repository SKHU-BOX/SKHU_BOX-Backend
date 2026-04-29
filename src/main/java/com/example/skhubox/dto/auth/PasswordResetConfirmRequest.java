package com.example.skhubox.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordResetConfirmRequest {

    @NotBlank(message = "학번은 필수 입력 사항입니다.")
    @Pattern(regexp = "^[0-9]{9}$", message = "학번은 9자리로 입력해주세요.")
    private String studentNumber;

    @NotBlank(message = "인증 코드는 필수 입력 사항입니다.")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 6자리 숫자입니다.")
    private String code;

    @NotBlank(message = "새 비밀번호는 필수 입력 사항입니다.")
    @Size(min = 4, message = "비밀번호는 최소 4자 이상이어야 합니다.")
    private String newPassword;
}
