package com.example.skhubox.repository;

import com.example.skhubox.domain.queue.QueueModeSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueModeSettingRepository extends JpaRepository<QueueModeSetting, Long> {
}