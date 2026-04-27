package com.example.skhubox.controller.admin;

import com.example.skhubox.dto.ApiResponse;
import com.example.skhubox.dto.notice.NoticeCreateRequest;
import com.example.skhubox.dto.notice.NoticeResponse;
import com.example.skhubox.dto.notice.NoticeUpdateRequest;
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

    @Operation(summary = "공지사항 수정", description = "공지사항을 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeResponse>> updateNotice(
            @PathVariable Long id,
            @RequestBody @Valid NoticeUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("공지사항이 수정되었습니다.", noticeService.updateNotice(id, request)));
    }

    @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.ok("공지사항이 삭제되었습니다.", null));
    }
}
