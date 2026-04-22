package com.example.skhubox.dto.dashboard;

import com.example.skhubox.dto.NotificationSettingResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminDashboardResponse {
    private String today;
    private NotificationSettingResponse notificationSetting;
    private long totalLockers;
    private long totalUsers;
    private String nearestExpiryDate;
    private long todayProcessedCount;
    private long unresolvedComplaintCount;
    private double totalUsageRate;
    private List<ProcessingHistory> todayProcessingHistory;
    private List<UsageRateByBuilding> buildingUsageRates;
    private List<RecentReservationActivity> recentReservationActivities;
    private List<UnresolvedComplaint> unresolvedComplaints;
    private OperationSummary operationSummary;

    @Getter
    @Builder
    public static class ProcessingHistory {
        private String title;
        private String description;
        private String createdAt;
    }

    @Getter
    @Builder
    public static class UsageRateByBuilding {
        private String building;
        private long totalLockers;
        private long activeLockers;
        private double usageRate;
    }

    @Getter
    @Builder
    public static class RecentReservationActivity {
        private String title;
        private String description;
        private String createdAt;
    }

    @Getter
    @Builder
    public static class UnresolvedComplaint {
        private Long id;
        private String lockerNumber;
        private String status;
        private String createdAt;
    }

    @Getter
    @Builder
    public static class OperationSummary {
        private long autoAssignedToday;
        private long brokenLockers;
        private long expiringSoon;
        private long newUsersThisWeek;
    }
}
