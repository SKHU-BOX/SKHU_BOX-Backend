package com.example.skhubox.dto.complaint;

import com.example.skhubox.domain.complaint.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintAnswerRequest {

    @NotNull(message = "상태를 선택해주세요.")
    private ComplaintStatus status;

    private String answer;
}
