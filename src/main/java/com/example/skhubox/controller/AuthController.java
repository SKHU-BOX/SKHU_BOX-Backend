package com.example.skhubox.controller;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.auth.EmailRequest;
import com.example.skhubox.dto.auth.EmailVerifyRequest;
import com.example.skhubox.dto.auth.LoginRequest;
import com.example.skhubox.dto.auth.LoginResponse;
import com.example.skhubox.dto.auth.PasswordResetConfirmRequest;
import com.example.skhubox.dto.auth.PasswordResetRequest;
import com.example.skhubox.dto.auth.SignupRequest;
import com.example.skhubox.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다. 이메일 인증이 선행되어야 합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.ok("회원가입 성공", null));
    }

    @Operation(summary = "이메일 인증 코드 발송", description = "입력한 이메일로 6자리 인증 코드를 발송합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 코드 발송 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 존재하는 이메일이거나 올바르지 않은 이메일 형식")
    })
    @PostMapping("/email/send")
    public ResponseEntity<ApiResponse<Void>> sendEmail(@Valid @RequestBody EmailRequest request) {
        authService.sendVerificationCode(request);
        return ResponseEntity.ok(ApiResponse.ok("인증 코드 발송 성공", null));
    }

    @Operation(summary = "이메일 인증 코드 검증", description = "발송된 인증 코드를 확인합니다.")
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody EmailVerifyRequest request) {
        authService.verifyCode(request);
        return ResponseEntity.ok(ApiResponse.ok("이메일 인증 성공", null));
    }

    @Operation(summary = "로그인", description = "학번과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("로그인 성공", response));
    }

    @Operation(summary = "비밀번호 재설정 요청", description = "학번과 이메일이 일치하면 비밀번호 재설정 토큰을 이메일로 발송합니다.")
    @PostMapping("/password-reset/request")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.ok("입력한 정보가 유효하면 재설정 메일을 발송했습니다.", null));
    }

    @Operation(summary = "비밀번호 재설정 완료", description = "이메일로 받은 토큰과 새 비밀번호를 입력해 비밀번호를 재설정합니다.")
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호가 성공적으로 변경되었습니다.", null));
    }
}
