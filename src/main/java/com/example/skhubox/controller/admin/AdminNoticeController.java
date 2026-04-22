package com.example.skhubox.controller.admin;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.notice.NoticeCreateRequest;
import com.example.skhubox.dto.notice.NoticeResponse;
import com.example.skhubox.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Notice", description = "관리자 공지사항 API")
@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지사항 등록", description = "새 공지사항을 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<NoticeResponse>> createNotice(@RequestBody @Valid NoticeCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("공지사항이 등록되었습니다.", noticeService.createNotice(request)));
    }
}
