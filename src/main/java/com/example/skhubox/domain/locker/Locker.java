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

    @Column(nullable = false, unique = true)
    private String lockerNumber;

    private String building;

    private int floor;

    private String locationDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LockerStatus status;

    public Locker(String lockerNumber, String building, int floor, String locationDetail) {
        this.lockerNumber = lockerNumber;
        this.building = building;
        this.floor = floor;
        this.locationDetail = locationDetail;
        this.status = LockerStatus.AVAILABLE;
    }

    public void reserve() {
        this.status = LockerStatus.RESERVED;
    }

    public void makeAvailable() {
        this.status = LockerStatus.AVAILABLE;
    }

    public void markBroken() {
        this.status = LockerStatus.BROKEN;
    }

    public void disable() {
        this.status = LockerStatus.DISABLED;
    }

    public boolean isAvailable() {
        return this.status == LockerStatus.AVAILABLE;
    }

    public boolean isBrokenOrDisabled() {
        return this.status == LockerStatus.BROKEN || this.status == LockerStatus.DISABLED;
    }
}