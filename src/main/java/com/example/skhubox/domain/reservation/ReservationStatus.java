package com.example.skhubox.domain.reservation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationStatus {
    ACTIVE("사용중"),
    RETURNED("반납됨"),
    CANCELLED("취소됨"),
    EXPIRED("만료됨");

    private final String description;
}
