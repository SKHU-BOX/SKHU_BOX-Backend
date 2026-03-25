package com.example.skhubox.controller;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.LockerChangeRequest;
import com.example.skhubox.dto.LockerReservationResponse;
import com.example.skhubox.dto.LockerReserveRequest;
import com.example.skhubox.service.LockerReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lockers")
public class LockerReservationController {

    private final LockerReservationService lockerReservationService;

    public LockerReservationController(LockerReservationService lockerReservationService) {
        this.lockerReservationService = lockerReservationService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> reserveLocker(
            @RequestBody LockerReserveRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        LockerReservationResponse response =
                lockerReservationService.reserveLocker(email, request.getLockerId());

        return ResponseEntity.ok(ApiResponse.ok("사물함 예약 성공", response));
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> returnLocker(
            Authentication authentication
    ) {
        String email = authentication.getName();
        LockerReservationResponse response =
                lockerReservationService.returnLocker(email);

        return ResponseEntity.ok(ApiResponse.ok("사물함 반납 성공", response));
    }

    @PostMapping("/change")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> changeLocker(
            @RequestBody LockerChangeRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        LockerReservationResponse response =
                lockerReservationService.changeLocker(email, request.getNewLockerId());

        return ResponseEntity.ok(ApiResponse.ok("사물함 변경 성공", response));
    }
}
