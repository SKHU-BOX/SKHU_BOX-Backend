package com.example.skhubox.service;

import com.example.skhubox.dto.LockerReservationResponse;

public interface LockerReservationService {

    LockerReservationResponse reserveLocker(String studentNumber, Long lockerId);

    LockerReservationResponse returnLocker(String email);

    LockerReservationResponse changeLocker(String email, Long newLockerId);
}