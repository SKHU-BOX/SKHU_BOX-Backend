package com.example.skhubox.service;

import com.example.skhubox.domain.complaint.Complaint;
import com.example.skhubox.domain.complaint.ComplaintStatus;
import com.example.skhubox.domain.locker.Locker;
import com.example.skhubox.domain.locker.LockerStatus;
import com.example.skhubox.domain.operation.OperationLog;
import com.example.skhubox.domain.operation.OperationLogType;
import com.example.skhubox.domain.reservation.LockerReservation;
import com.example.skhubox.domain.reservation.ReservationStatus;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.NotificationSettingResponse;
import com.example.skhubox.dto.dashboard.AdminDashboardResponse;
import com.example.skhubox.dto.dashboard.UserDashboardResponse;
import com.example.skhubox.dto.notice.NoticeResponse;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.exception.ErrorCode;
import com.example.skhubox.repository.ComplaintRepository;
import com.example.skhubox.repository.LockerRepository;
import com.example.skhubox.repository.LockerReservationRepository;
import com.example.skhubox.repository.NoticeRepository;
import com.example.skhubox.repository.OperationLogRepository;
import com.example.skhubox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final UserRepository userRepository;
    private final LockerRepository lockerRepository;
    private final LockerReservationRepository lockerReservationRepository;
    private final ComplaintRepository complaintRepository;
    private final NoticeRepository noticeRepository;
    private final OperationLogRepository operationLogRepository;
    private final ReservationExpirationService reservationExpirationService;

    public UserDashboardResponse getUserDashboard(String studentNumber) {
        reservationExpirationService.expireOverdueReservations();

        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserDashboardResponse.MyLockerCard myLocker = lockerReservationRepository
                .findByUser_IdAndStatusAndExpiredAtAfter(user.getId(), ReservationStatus.ACTIVE, LocalDateTime.now())
                .map(this::toUserLockerCard)
                .orElse(UserDashboardResponse.MyLockerCard.builder()
                        .reserved(false)
                        .reservationStatus("NONE")
                        .build());

        List<UserDashboardResponse.MyComplaintCard> myComplaints = complaintRepository.findByUserId(user.getId())
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(complaint -> UserDashboardResponse.MyComplaintCard.builder()
                        .id(complaint.getId())
                        .lockerNumber(complaint.getLockerNumber())
                        .status(complaint.getStatus().getDescription())
                        .createdAt(complaint.getCreatedAt().format(DATE_TIME_FORMATTER))
                        .build())
                .toList();

        List<NoticeResponse> notices = noticeRepository.findTop5ByOrderByPinnedDescCreatedAtDesc()
                .stream()
                .map(NoticeResponse::from)
                .toList();

        return UserDashboardResponse.builder()
                .today(LocalDate.now().format(DATE_FORMATTER))
                .notificationSetting(new NotificationSettingResponse(user.isNotificationEnabled()))
                .myLocker(myLocker)
                .totalUsers(userRepository.count())
                .totalAvailableLockers(lockerRepository.countByStatus(LockerStatus.NORMAL))
                .notices(notices)
                .myComplaints(myComplaints)
                .build();
    }

    public AdminDashboardResponse getAdminDashboard(String studentNumber) {
        reservationExpirationService.expireOverdueReservations();

        User admin = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.plusDays(1).atStartOfDay();
        LocalDateTime endOfSoon = LocalDateTime.now().plusDays(7);
        LocalDateTime startOfWeek = today.minusDays(6).atStartOfDay();

        List<Locker> lockers = lockerRepository.findAll();
        List<LockerReservation> activeReservations = lockerReservationRepository.findAllByStatus(ReservationStatus.ACTIVE);
        List<ComplaintStatus> unresolvedStatuses = List.of(
                ComplaintStatus.PENDING,
                ComplaintStatus.UNDER_REVIEW,
                ComplaintStatus.IN_PROGRESS
        );

        long totalLockers = lockers.size();
        long activeLockers = lockers.stream().filter(locker -> locker.getStatus() == LockerStatus.ACTIVE).count();
        double totalUsageRate = totalLockers == 0 ? 0 : (activeLockers * 100.0) / totalLockers;

        String nearestExpiryDate = activeReservations.stream()
                .map(LockerReservation::getExpiredAt)
                .min(LocalDateTime::compareTo)
                .map(date -> date.format(DATE_TIME_FORMATTER))
                .orElse(null);

        long todayProcessedCount = operationLogRepository.countByTypeInAndCreatedAtBetween(
                List.of(
                        OperationLogType.RESERVATION_ASSIGNED,
                        OperationLogType.RESERVATION_RETURNED,
                        OperationLogType.RESERVATION_CHANGED,
                        OperationLogType.RESERVATION_EXPIRED,
                        OperationLogType.COMPLAINT_PROCESSED
                ),
                startOfToday,
                endOfToday
        );

        List<AdminDashboardResponse.ProcessingHistory> todayProcessingHistory = operationLogRepository
                .findTop10ByCreatedAtBetweenOrderByCreatedAtDesc(startOfToday, endOfToday)
                .stream()
                .map(this::toProcessingHistory)
                .toList();

        Map<String, List<Locker>> lockersByBuilding = lockers.stream()
                .collect(Collectors.groupingBy(Locker::getBuilding));

        List<AdminDashboardResponse.UsageRateByBuilding> buildingUsageRates = lockersByBuilding.entrySet()
                .stream()
                .map(entry -> {
                    long buildingTotal = entry.getValue().size();
                    long buildingActive = entry.getValue().stream()
                            .filter(locker -> locker.getStatus() == LockerStatus.ACTIVE)
                            .count();
                    return AdminDashboardResponse.UsageRateByBuilding.builder()
                            .building(entry.getKey())
                            .totalLockers(buildingTotal)
                            .activeLockers(buildingActive)
                            .usageRate(buildingTotal == 0 ? 0 : (buildingActive * 100.0) / buildingTotal)
                            .build();
                })
                .sorted((a, b) -> a.getBuilding().compareToIgnoreCase(b.getBuilding()))
                .toList();

        List<AdminDashboardResponse.RecentReservationActivity> recentReservationActivities = operationLogRepository
                .findTop4ByTypeInOrderByCreatedAtDesc(List.of(
                        OperationLogType.RESERVATION_ASSIGNED,
                        OperationLogType.RESERVATION_RETURNED,
                        OperationLogType.RESERVATION_CHANGED,
                        OperationLogType.RESERVATION_EXPIRED
                ))
                .stream()
                .map(log -> AdminDashboardResponse.RecentReservationActivity.builder()
                        .title(log.getTitle())
                        .description(log.getDescription())
                        .createdAt(log.getCreatedAt().format(DATE_TIME_FORMATTER))
                        .build())
                .toList();

        List<AdminDashboardResponse.UnresolvedComplaint> unresolvedComplaints = complaintRepository
                .findTop4ByStatusInOrderByCreatedAtDesc(unresolvedStatuses)
                .stream()
                .map(complaint -> AdminDashboardResponse.UnresolvedComplaint.builder()
                        .id(complaint.getId())
                        .lockerNumber(complaint.getLockerNumber())
                        .status(complaint.getStatus().getDescription())
                        .createdAt(complaint.getCreatedAt().format(DATE_TIME_FORMATTER))
                        .build())
                .toList();

        long expiringSoon = activeReservations.stream()
                .filter(reservation -> !reservation.getExpiredAt().isBefore(LocalDateTime.now()))
                .filter(reservation -> !reservation.getExpiredAt().isAfter(endOfSoon))
                .count();

        return AdminDashboardResponse.builder()
                .today(today.format(DATE_FORMATTER))
                .notificationSetting(new NotificationSettingResponse(admin.isNotificationEnabled()))
                .totalLockers(totalLockers)
                .totalUsers(userRepository.count())
                .nearestExpiryDate(nearestExpiryDate)
                .todayProcessedCount(todayProcessedCount)
                .unresolvedComplaintCount(complaintRepository.countByStatusIn(unresolvedStatuses))
                .totalUsageRate(totalUsageRate)
                .todayProcessingHistory(todayProcessingHistory)
                .buildingUsageRates(buildingUsageRates)
                .recentReservationActivities(recentReservationActivities)
                .unresolvedComplaints(unresolvedComplaints)
                .operationSummary(AdminDashboardResponse.OperationSummary.builder()
                        .autoAssignedToday(operationLogRepository.countByTypeInAndCreatedAtBetween(
                                List.of(OperationLogType.RESERVATION_ASSIGNED), startOfToday, endOfToday))
                        .brokenLockers(lockers.stream().filter(locker -> locker.getStatus() == LockerStatus.BROKEN).count())
                        .expiringSoon(expiringSoon)
                        .newUsersThisWeek(userRepository.countByCreatedAtBetween(startOfWeek, endOfToday))
                        .build())
                .build();
    }

    private UserDashboardResponse.MyLockerCard toUserLockerCard(LockerReservation reservation) {
        long remainingDays = Math.max(0, ChronoUnit.DAYS.between(LocalDateTime.now(), reservation.getExpiredAt()));
        long usageDays = Math.max(0, ChronoUnit.DAYS.between(reservation.getReservedAt(), LocalDateTime.now()));
        Locker locker = reservation.getLocker();

        return UserDashboardResponse.MyLockerCard.builder()
                .reserved(true)
                .reservationStatus(reservation.getStatus().getDescription())
                .lockerNumber(locker.getLockerNumber())
                .location(String.format("%s %d층 %s", locker.getBuilding(), locker.getFloor(), locker.getLocationDetail()))
                .lockerStatus(locker.getStatus().getDescription())
                .reservedAt(reservation.getReservedAt().format(DATE_TIME_FORMATTER))
                .expiredAt(reservation.getExpiredAt().format(DATE_TIME_FORMATTER))
                .remainingDays(remainingDays)
                .usageDays(usageDays)
                .build();
    }

    private AdminDashboardResponse.ProcessingHistory toProcessingHistory(OperationLog log) {
        return AdminDashboardResponse.ProcessingHistory.builder()
                .title(log.getTitle())
                .description(log.getDescription())
                .createdAt(log.getCreatedAt().format(DATE_TIME_FORMATTER))
                .build();
    }
}
