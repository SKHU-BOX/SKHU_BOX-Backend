package com.example.skhubox.service;

import com.example.skhubox.domain.locker.Locker;
import com.example.skhubox.domain.reservation.LockerReservation;
import com.example.skhubox.domain.reservation.ReservationStatus;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.LockerReservationResponse;
import com.example.skhubox.dto.LockerResponse;
import com.example.skhubox.dto.QueueResponse;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.exception.ErrorCode;
import com.example.skhubox.repository.LockerRepository;
import com.example.skhubox.repository.UserRepository;
import com.example.skhubox.repository.LockerReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LockerReservationServiceImpl implements LockerReservationService {

    private final UserRepository userRepository;
    private final LockerRepository lockerRepository;
    private final LockerReservationRepository lockerReservationRepository;
    private final QueueModeSettingService queueModeSettingService;
    private final WaitingQueueService waitingQueueService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String LOCK_PREFIX = "lock:locker:";

    @Override
    public LockerReservationResponse reserveLocker(String studentNumber, Long lockerId) {
        String lockKey = LOCK_PREFIX + lockerId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 5, TimeUnit.SECONDS);
        
        if (Boolean.FALSE.equals(acquired)) {
            log.warn("[Concurrency] Lock acquisition failed for locker {} by user {}", lockerId, studentNumber);
            throw new BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED);
        }

        try {
            if (queueModeSettingService.isQueueModeEnabled()) {
                Long myRank = waitingQueueService.getRank(studentNumber, lockerId);

                if (myRank == null) {
                    QueueResponse queueResponse = waitingQueueService.register(studentNumber, lockerId);
                    log.info("[Queue] User {} auto-registered for locker {}. Rank: {}", 
                            studentNumber, lockerId, queueResponse.getRank());
                    throw new BusinessException(ErrorCode.QUEUE_MODE_RESERVATION_BLOCKED,
                            "현재 대기열 모드입니다. 대기열에 등록되었습니다. 내 순번: " + queueResponse.getRank() + "번");
                }

                if (!waitingQueueService.isFirstUser(studentNumber, lockerId)) {
                    log.warn("[Queue] User {} tried to reserve but is NOT first for locker {}. My Rank: {}", 
                            studentNumber, lockerId, myRank);
                    throw new BusinessException(ErrorCode.QUEUE_MODE_RESERVATION_BLOCKED,
                            "아직 본인 차례가 아닙니다. 현재 순번: " + myRank + "번");
                }
            }

            User user = getUser(studentNumber);
            Locker locker = getLockedLocker(lockerId);

            validateReservable(user, locker);

            try {
                LockerReservation reservation = new LockerReservation(user, locker);
                LockerReservation savedReservation = lockerReservationRepository.saveAndFlush(reservation);

                if (queueModeSettingService.isQueueModeEnabled()) {
                    waitingQueueService.removeFromQueue(studentNumber, lockerId);
                }

                log.info("[Reservation-Success] User {} successfully reserved locker {}.", studentNumber, lockerId);
                return toResponse(savedReservation, "사물함 예약이 완료되었습니다.");
            } catch (DataIntegrityViolationException e) {
                log.error("[Reservation-Failed] Concurrent DB reservation for locker {}: {}", lockerId, e.getMessage());
                throw new BusinessException(ErrorCode.ALREADY_RESERVED_LOCKER);
            }
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public LockerReservationResponse returnLocker(String studentNumber) {
        User user = getUser(studentNumber);

        LockerReservation reservation = lockerReservationRepository
                .findByUser_IdAndStatus(user.getId(), ReservationStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_ACTIVE_RESERVATION));

        Long lockerId = reservation.getLocker().getId();
        getLockedLocker(lockerId);

        reservation.returnReservation();

        log.info("[Return-Success] User {} returned locker {}.", studentNumber, lockerId);
        return toResponse(reservation, "사물함 반납이 완료되었습니다.");
    }

    @Override
    public LockerReservationResponse changeLocker(String studentNumber, Long newLockerId) {
        User user = getUser(studentNumber);

        LockerReservation currentReservation = lockerReservationRepository
                .findByUser_IdAndStatus(user.getId(), ReservationStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_ACTIVE_RESERVATION));

        Long currentLockerId = currentReservation.getLocker().getId();

        if (currentLockerId.equals(newLockerId)) {
            throw new BusinessException(ErrorCode.SAME_LOCKER_CHANGE);
        }

        Long firstId = Math.min(currentLockerId, newLockerId);
        Long secondId = Math.max(currentLockerId, newLockerId);

        getLockedLocker(firstId);
        Locker secondLocker = getLockedLocker(secondId);

        Locker newLocker = (firstId.equals(newLockerId)) ? getLockedLocker(firstId) : secondLocker;

        validateNewLocker(newLocker);

        try {
            currentReservation.returnReservation();
            LockerReservation newReservation = new LockerReservation(user, newLocker);
            LockerReservation savedReservation = lockerReservationRepository.saveAndFlush(newReservation);
            
            log.info("[Change-Success] User {} changed locker from {} to {}.", studentNumber, currentLockerId, newLockerId);
            return toResponse(savedReservation, "사물함 변경이 완료되었습니다.");
        } catch (DataIntegrityViolationException e) {
            log.error("[Change-Failed] Concurrent DB change to locker {}: {}", newLockerId, e.getMessage());
            throw new BusinessException(ErrorCode.ALREADY_RESERVED_LOCKER);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LockerResponse> getAllLockers() {
        return lockerRepository.findAll().stream()
                .map(LockerResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LockerReservationResponse getMyReservation(String studentNumber) {
        User user = getUser(studentNumber);

        LockerReservation reservation = lockerReservationRepository
                .findByUser_IdAndStatus(user.getId(), ReservationStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_ACTIVE_RESERVATION));

        return toResponse(reservation, "현재 예약 정보 조회 성공");
    }

    @Override
    public void updateExpiryDate(Long reservationId, LocalDateTime newExpiryDate) {
        LockerReservation reservation = lockerReservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        
        reservation.updateExpiryDate(newExpiryDate);
        log.info("[Admin] Updated expiry date for reservation {} to {}", reservationId, newExpiryDate);
    }

    // --- Private Helper Methods ---

    private User getUser(String studentNumber) {
        return userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Locker getLockedLocker(Long lockerId) {
        return lockerRepository.findByIdWithPessimisticLock(lockerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOCKER_NOT_FOUND));
    }

    private void validateReservable(User user, Locker locker) {
        if (!locker.isNormal()) {
            throw new BusinessException(ErrorCode.LOCKER_NOT_NORMAL);
        }

        if (lockerReservationRepository.existsByUser_IdAndStatus(user.getId(), ReservationStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.USER_ALREADY_HAS_LOCKER);
        }

        if (lockerReservationRepository.existsByLocker_IdAndStatus(locker.getId(), ReservationStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.ALREADY_RESERVED_LOCKER);
        }
    }

    private void validateNewLocker(Locker newLocker) {
        if (!newLocker.isNormal()) {
            throw new BusinessException(ErrorCode.LOCKER_NOT_NORMAL);
        }

        if (lockerReservationRepository.existsByLocker_IdAndStatus(newLocker.getId(), ReservationStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.ALREADY_RESERVED_LOCKER);
        }
    }

    private LockerReservationResponse toResponse(LockerReservation reservation, String message) {
        return new LockerReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getLocker().getId(),
                reservation.getStatus().name(),
                reservation.getExpiredAt() != null ? reservation.getExpiredAt().toString() : null,
                message
        );
    }
}
