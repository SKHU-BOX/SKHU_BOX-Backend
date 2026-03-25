package com.example.skhubox.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LockerReservationResponse {

    private Long reservationId;
    private Long userId;
    private Long lockerId;
    private String status;
    private String message;

    public LockerReservationResponse() {
    }

    public LockerReservationResponse(Long reservationId, Long userId, Long lockerId, String status, String message) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.lockerId = lockerId;
        this.status = status;
        this.message = message;
    }

}