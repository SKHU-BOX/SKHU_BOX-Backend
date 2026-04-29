package com.example.skhubox.dto.dashboard;

import com.example.skhubox.dto.NotificationSettingResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserDashboardResponse {
    private String today;
    private NotificationSettingResponse notificationSetting;
    private MyLockerCard myLocker;
    private long totalUsers;
    private long totalAvailableLockers;
    private List<MyComplaintCard> myComplaints;

    @Getter
    @Builder
    public static class MyLockerCard {
        private boolean reserved;
        private String reservationStatus;
        private String lockerNumber;
        private String location;
        private String lockerStatus;
        private String reservedAt;
        private String expiredAt;
        private long remainingDays;
        private long usageDays;
    }

    @Getter
    @Builder
    public static class MyComplaintCard {
        private Long id;
        private String lockerNumber;
        private String status;
        private String createdAt;
    }
}
