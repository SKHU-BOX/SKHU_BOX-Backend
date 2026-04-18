package com.example.skhubox.controller;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.DashboardResponse;
import com.example.skhubox.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard API", description = "메인 화면용 대시보드 API")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "대시보드 데이터 조회", description = "로그인한 사용자의 사물함 정보 및 시스템 통계 데이터를 반환합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        DashboardResponse response = dashboardService.getDashboardData(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("대시보드 데이터 조회를 성공했습니다.", response));
    }
}
