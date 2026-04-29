package com.example.skhubox.controller;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.FcmTokenRequest;
import com.example.skhubox.dto.NotificationSettingRequest;
import com.example.skhubox.dto.NotificationSettingResponse;
import com.example.skhubox.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User API", description = "사용자 정보 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "FCM 토큰 등록", description = "푸시 알림을 위한 기기 토큰을 등록합니다.")
    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody FcmTokenRequest request) {
        
        userService.updateFcmToken(userDetails.getUsername(), request.getToken());
        return ResponseEntity.ok(ApiResponse.ok("FCM 토큰이 성공적으로 등록되었습니다.", null));
    }

    @Operation(summary = "알림 설정 변경", description = "사용자의 알림 수신 여부를 변경합니다.")
    @PatchMapping("/notification-setting")
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> updateNotificationSetting(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody NotificationSettingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                "알림 설정이 변경되었습니다.",
                userService.updateNotificationSetting(userDetails.getUsername(), request.isEnabled())
        ));
    }
}
