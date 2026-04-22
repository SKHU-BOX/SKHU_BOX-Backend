package com.example.skhubox.dto.notice;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NoticeCreateRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private boolean pinned;
}
