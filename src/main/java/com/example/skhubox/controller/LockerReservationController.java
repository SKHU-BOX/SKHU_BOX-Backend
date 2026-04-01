package com.example.skhubox.controller;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.LockerChangeRequest;
import com.example.skhubox.dto.LockerReservationResponse;
import com.example.skhubox.dto.LockerResponse;
import com.example.skhubox.dto.LockerReserveRequest;
import com.example.skhubox.security.CustomUserDetails;
import com.example.skhubox.service.LockerReservationService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lockers")
public class LockerReservationController {

    private final LockerReservationService lockerReservationService;

    public LockerReservationController(LockerReservationService lockerReservationService) {
        this.lockerReservationService = lockerReservationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LockerResponse>>> getAllLockers() {
        List<LockerResponse> response = lockerReservationService.getAllLockers();
        return ResponseEntity.ok(ApiResponse.ok("전체 사물함 목록 조회 성공", response));
    }

    @GetMapping("/my-reservation")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> getMyReservation(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String studentNumber = userDetails.getUsername();
        LockerReservationResponse response = lockerReservationService.getMyReservation(studentNumber);
        return ResponseEntity.ok(ApiResponse.ok("내 예약 정보 조회 성공", response));
    }

    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> reserveLocker(
            @RequestBody LockerReserveRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String studentNumber = userDetails.getUsername();

        LockerReservationResponse response =
                lockerReservationService.reserveLocker(studentNumber, request.getLockerId());

        return ResponseEntity.ok(ApiResponse.ok("사물함 예약 성공", response));
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> returnLocker(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String studentNumber = userDetails.getUsername();
        LockerReservationResponse response =
                lockerReservationService.returnLocker(studentNumber);

        return ResponseEntity.ok(ApiResponse.ok("사물함 반납 성공", response));
    }

    @PostMapping("/change")
    public ResponseEntity<ApiResponse<LockerReservationResponse>> changeLocker(
            @RequestBody LockerChangeRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String studentNumber = userDetails.getUsername();
        LockerReservationResponse response =
                lockerReservationService.changeLocker(studentNumber, request.getNewLockerId());

        return ResponseEntity.ok(ApiResponse.ok("사물함 변경 성공", response));
    }
}
