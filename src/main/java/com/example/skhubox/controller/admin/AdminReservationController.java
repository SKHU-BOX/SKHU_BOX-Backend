package com.example.skhubox.controller.admin;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.service.LockerReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Admin Reservation API", description = "관리자용 예약 관리 API")
@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReservationController {

    private final LockerReservationService lockerReservationService;

    @Operation(summary = "예약 만료일 수정", description = "특정 예약의 만료 기한을 관리자가 직접 수정합니다.")
    @PatchMapping("/{reservationId}/expiry")
    public ResponseEntity<ApiResponse<Void>> updateExpiryDate(
            @PathVariable Long reservationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newExpiryDate) {
        
        lockerReservationService.updateExpiryDate(reservationId, newExpiryDate);
        return ResponseEntity.ok(ApiResponse.ok("만료일이 성공적으로 수정되었습니다.", null));
    }
}
