package com.example.skhubox.dto.admin;

import com.example.skhubox.domain.user.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRoleUpdateRequest {
    @NotBlank(message = "대상 학번은 필수 입력값입니다.")
    private String targetStudentNumber;

    @NotNull(message = "변경할 권한은 필수 입력값입니다.")
    private UserRole role;
}
