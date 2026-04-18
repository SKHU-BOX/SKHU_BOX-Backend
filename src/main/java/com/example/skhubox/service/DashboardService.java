package com.example.skhubox.service;

import com.example.skhubox.domain.locker.LockerStatus;
import com.example.skhubox.domain.reservation.LockerReservation;
import com.example.skhubox.domain.reservation.ReservationStatus;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.DashboardResponse;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.exception.ErrorCode;
import com.example.skhubox.repository.ComplaintRepository;
import com.example.skhubox.repository.LockerRepository;
import com.example.skhubox.repository.LockerReservationRepository;
import com.example.skhubox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserRepository userRepository;
    private final LockerRepository lockerRepository;
    private final LockerReservationRepository lockerReservationRepository;
    private final ComplaintRepository complaintRepository;

    public DashboardResponse getDashboardData(String studentNumber) {
        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 1. 내 예약 정보 조회
        DashboardResponse.MyLockerInfo myLockerInfo = lockerReservationRepository
                .findByUser_IdAndStatus(user.getId(), ReservationStatus.ACTIVE)
                .map(this::mapToMyLockerInfo)
                .orElse(null);

        // 2. 통계 데이터 합산
        return DashboardResponse.builder()
                .myLocker(myLockerInfo)
                .totalAvailableLockers(lockerRepository.countByStatus(LockerStatus.NORMAL))
                .totalUsers(userRepository.count())
                .totalComplaints(complaintRepository.count())
                .build();
    }

    private DashboardResponse.MyLockerInfo mapToMyLockerInfo(LockerReservation reservation) {
        long remainingDays = ChronoUnit.DAYS.between(LocalDateTime.now(), reservation.getExpiredAt());
        
        return DashboardResponse.MyLockerInfo.builder()
                .lockerId(reservation.getLocker().getId())
                .lockerNumber(reservation.getLocker().getLockerNumber())
                .building(reservation.getLocker().getBuilding())
                .expiredAt(reservation.getExpiredAt().toString())
                .remainingDays(Math.max(0, remainingDays))
                .build();
    }
}
