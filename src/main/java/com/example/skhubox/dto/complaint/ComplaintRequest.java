package com.example.skhubox.dto.complaint;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintRequest {

    @NotBlank(message = "사물함 번호를 입력해주세요.")
    private String lockerNumber;

    @NotBlank(message = "문의 내용을 입력해주세요.")
    private String content;
}
