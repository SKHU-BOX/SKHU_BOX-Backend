package com.example.skhubox.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "올바르지 않은 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다."),

    // 인증/학번
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 사용자입니다."),
    DUPLICATE_STUDENT_NUMBER(HttpStatus.BAD_REQUEST, "U002", "이미 존재하는 학번입니다."),
    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "U003", "이미 존재하는 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "U004", "비밀번호가 일치하지 않습니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "U005", "로그인 인증에 실패했습니다."),

    // 사물함
    LOCKER_NOT_FOUND(HttpStatus.NOT_FOUND, "L001", "사물함 정보가 존재하지 않습니다."),
    LOCKER_NOT_NORMAL(HttpStatus.BAD_REQUEST, "L002", "해당 사물함은 현재 사용 불가 상태입니다."),
    ALREADY_RESERVED_LOCKER(HttpStatus.CONFLICT, "L003", "이미 다른 사용자가 이용 중인 사물함입니다."),
    USER_ALREADY_HAS_LOCKER(HttpStatus.BAD_REQUEST, "L004", "이미 이용 중인 사물함이 있습니다."),
    NO_ACTIVE_RESERVATION(HttpStatus.NOT_FOUND, "L005", "현재 이용 중인 사물함 예약 내역이 없습니다."),
    SAME_LOCKER_CHANGE(HttpStatus.BAD_REQUEST, "L006", "동일한 사물함으로 변경할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
