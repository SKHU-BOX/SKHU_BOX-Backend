package com.example.skhubox.domain.user;

import com.example.skhubox.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class AdminActionLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String adminStudentNumber; // 권한을 변경한 관리자 학번
    private String targetStudentNumber; // 대상 사용자 학번
    private String actionType; // ROLE_UPGRADE, ROLE_DOWNGRADE 등
    private String details; // 상세 설명

    public AdminActionLog(String adminStudentNumber, String targetStudentNumber, String actionType, String details) {
        this.adminStudentNumber = adminStudentNumber;
        this.targetStudentNumber = targetStudentNumber;
        this.actionType = actionType;
        this.details = details;
    }
}
