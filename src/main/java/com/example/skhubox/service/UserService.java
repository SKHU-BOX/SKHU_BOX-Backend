package com.example.skhubox.service;
import com.example.skhubox.domain.user.AdminActionLog;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.domain.user.UserRole;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.exception.ErrorCode;
import com.example.skhubox.repository.AdminActionLogRepository;
import com.example.skhubox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final AdminActionLogRepository adminActionLogRepository;

    public User findByStudentNumber(String studentNumber) {
        return userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void updateUserRole(String adminStudentNumber, String targetStudentNumber, UserRole newRole) {
        User targetUser = findByStudentNumber(targetStudentNumber);

        String actionType = newRole == UserRole.ADMIN ? "ROLE_UPGRADE" : "ROLE_DOWNGRADE";
        String details = String.format("Role changed from %s to %s", targetUser.getRole(), newRole);

        if (newRole == UserRole.ADMIN) {
            targetUser.assignAdminRole();
        } else {
            targetUser.assignUserRole();
        }

        // 로그 저장
        AdminActionLog log = new AdminActionLog(adminStudentNumber, targetStudentNumber, actionType, details);
        adminActionLogRepository.save(log);
    }

    public boolean existsByStudentNumber(String studentNumber) {
...
        return userRepository.existsByStudentNumber(studentNumber);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
