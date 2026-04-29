package com.example.skhubox.domain.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OperationLogType {
    RESERVATION_ASSIGNED("예약 배정"),
    RESERVATION_RETURNED("예약 반납"),
    RESERVATION_CHANGED("예약 변경"),
    RESERVATION_EXPIRED("예약 만료"),
    COMPLAINT_SUBMITTED("민원 접수"),
    COMPLAINT_PROCESSED("민원 처리"),
    NOTICE_POSTED("공지 등록"),
    NOTICE_UPDATED("공지 수정"),
    NOTICE_DELETED("공지 삭제"),
    USER_WITHDRAWN("회원 탈퇴");

    private final String description;
}
