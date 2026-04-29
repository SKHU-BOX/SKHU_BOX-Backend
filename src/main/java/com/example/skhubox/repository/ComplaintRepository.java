package com.example.skhubox.repository;

import com.example.skhubox.domain.complaint.Complaint;
import com.example.skhubox.domain.complaint.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByUserId(Long userId);
    long countByStatusIn(List<ComplaintStatus> statuses);
    List<Complaint> findTop4ByStatusInOrderByCreatedAtDesc(List<ComplaintStatus> statuses);
    long countByStatusAndUpdatedAtBetween(ComplaintStatus status, LocalDateTime start, LocalDateTime end);
}
