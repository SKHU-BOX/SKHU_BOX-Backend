package com.example.skhubox.domain.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    RESERVATION("예약"),
    COMPLAINT("민원"),
    SYSTEM("시스템"),
    EXPIRY("만료안내");

    private final String description;
}
