package com.example.skhubox.repository;

import com.example.skhubox.domain.notice.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findTop5ByOrderByPinnedDescCreatedAtDesc();
}
