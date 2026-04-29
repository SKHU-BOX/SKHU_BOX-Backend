package com.example.skhubox.controller;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.dashboard.AdminDashboardResponse;
import com.example.skhubox.dto.dashboard.UserDashboardResponse;
import com.example.skhubox.security.CustomUserDetails;
import com.example.skhubox.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Dashboard API", description = "메인 화면용 대시보드 API")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "사용자 대시보드 조회", description = "일반 사용자 대시보드 데이터를 조회합니다.")
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<UserDashboardResponse>> getUserDashboard(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                "사용자 대시보드 조회 성공",
                dashboardService.getUserDashboard(userDetails.getUsername())
        ));
    }

    @Operation(summary = "관리자 대시보드 조회", description = "관리자 대시보드 데이터를 조회합니다.")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                "관리자 대시보드 조회 성공",
                dashboardService.getAdminDashboard(userDetails.getUsername())
        ));
    }
}
