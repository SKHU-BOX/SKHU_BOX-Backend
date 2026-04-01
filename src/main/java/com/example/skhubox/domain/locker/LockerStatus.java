package com.example.skhubox.domain.locker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LockerStatus {
    NORMAL("정상"),
    BROKEN("고장"),
    DISABLED("사용불가");

    private final String description;
}
