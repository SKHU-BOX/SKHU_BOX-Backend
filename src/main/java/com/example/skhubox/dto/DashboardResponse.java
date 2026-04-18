package com.example.skhubox.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardResponse {
    // 내 사물함 정보
    private MyLockerInfo myLocker;
    
    // 통계 데이터
    private long totalAvailableLockers; // 전체 잔여 사물함 수
    private long totalUsers;            // 전체 사용자 수
    private long totalComplaints;       // 전체 민원 수 (또는 처리 대기 중인 민원 수)

    @Getter
    @Builder
    public static class MyLockerInfo {
        private Long lockerId;
        private String lockerNumber;
        private String building;
        private String expiredAt;
        private long remainingDays; // 남은 기간 (일 단위)
    }
}
