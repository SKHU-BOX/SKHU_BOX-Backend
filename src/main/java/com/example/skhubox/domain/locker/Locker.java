package com.example.skhubox.domain.locker;

import com.example.skhubox.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "lockers")
public class Locker extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String building;

    @Column(nullable = false, unique = true)
    private String lockerNumber;

    @Column(nullable = false)
    private int floor;

    @Column(nullable = false)
    private String locationDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LockerStatus status;

    public Locker(String building, int floor, String locationDetail, String lockerNumber) {
        this.building = building;
        this.floor = floor;
        this.locationDetail = locationDetail;
        this.lockerNumber = lockerNumber;
        this.status = LockerStatus.NORMAL;
    }

    // 물리적 상태 변경 로직 (관리자용)
    public void markBroken() {
        this.status = LockerStatus.BROKEN;
    }

    public void disable() {
        this.status = LockerStatus.DISABLED;
    }

    public void restore() {
        this.status = LockerStatus.NORMAL;
    }

    // 대여 가능 여부 중 '물리적 상태'만 체크
    public boolean isNormal() {
        return this.status == LockerStatus.NORMAL;
    }
}
