package com.example.skhubox.repository;

import com.example.skhubox.domain.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
}
