package com.example.skhubox.service;

import com.example.skhubox.dto.LockerReservationResponse;

public interface LockerReservationService {

    LockerReservationResponse reserveLocker(Long userId, Long lockerId);

    LockerReservationResponse returnLocker(Long userId);

    LockerReservationResponse changeLocker(Long userId, Long newLockerId);
}