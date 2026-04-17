package com.example.skhubox.controller.admin;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.admin.UserRoleUpdateRequest;
import com.example.skhubox.security.CustomUserDetails;
import com.example.skhubox.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin User", description = "관리자용 사용자 관리 API")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "사용자 권한 변경", description = "특정 사용자의 권한을 ADMIN 또는 USER로 변경합니다.")
    @PatchMapping("/role")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @Valid @RequestBody UserRoleUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails adminDetails
    ) {
        String adminStudentNumber = adminDetails.getUsername();
        userService.updateUserRole(adminStudentNumber, request.getTargetStudentNumber(), request.getRole());
        
        String message = request.getRole().name() + " 권한으로 변경되었습니다.";
        return ResponseEntity.ok(ApiResponse.ok(message, null));
    }
}
