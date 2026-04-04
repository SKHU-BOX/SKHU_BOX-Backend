package com.example.skhubox.dto;

import com.example.skhubox.domain.locker.Locker;
import com.example.skhubox.domain.locker.LockerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockerResponse {
    private Long id;
    private String building;
    private int floor;
    private String locationDetail;
    private String lockerNumber;
    private LockerStatus status;

    public static LockerResponse from(Locker locker) {
        return LockerResponse.builder()
                .id(locker.getId())
                .building(locker.getBuilding())
                .floor(locker.getFloor())
                .locationDetail(locker.getLocationDetail())
                .lockerNumber(locker.getLockerNumber())
                .status(locker.getStatus())
                .build();
    }
}
