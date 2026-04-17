package com.example.skhubox.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailVerifyRequest {
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @jakarta.validation.constraints.Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@office\\.skhu\\.ac\\.kr$",
            message = "학교 이메일(@office.skhu.ac.kr)만 사용 가능합니다."
    )
    private String email;

    @NotBlank(message = "인증 코드는 필수 입력값입니다.")
    private String code;
}
