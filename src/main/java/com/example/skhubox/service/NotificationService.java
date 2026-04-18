package com.example.skhubox.service;

import com.example.skhubox.domain.notification.Notification;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.NotificationResponse;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.exception.ErrorCode;
import com.example.skhubox.repository.NotificationRepository;
import com.example.skhubox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationResponse> getNotifications(String studentNumber) {
        User user = getUserByStudentNumber(studentNumber);
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String studentNumber) {
        User user = getUserByStudentNumber(studentNumber);
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)); // 알림 없음
        notification.markAsRead();
    }

    @Transactional
    public void createNotification(User user, String title, String content, com.example.skhubox.domain.notification.NotificationType type) {
        Notification notification = new Notification(user, title, content, type);
        notificationRepository.save(notification);
    }

    private User getUserByStudentNumber(String studentNumber) {
        return userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
