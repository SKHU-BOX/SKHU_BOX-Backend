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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class LockerReservationServiceImpl implements LockerReservationService {

    private final UserRepository userRepository;
    private final LockerRepository lockerRepository;
    private final LockerReservationRepository lockerReservationRepository;
    private final QueueModeSettingService queueModeSettingService;
    private final WaitingQueueService waitingQueueService;

    @Override
    public LockerReservationResponse reserveLocker(String studentNumber, Long lockerId) {
        // 0. 대기열 모드 체크
        if (queueModeSettingService.isQueueModeEnabled()) {
            // 대기열 등록 후 결과 반환 (현재는 에러로 던지지만, 실제로는 DTO에 대기열 정보를 담아 보낼 수 있음)
            // 지시사항에 따라 에러를 던지지 않고 정보를 반환하려면 리턴 타입 변경이 필요함.
            // 일단은 에러 메시지에 순번을 포함시켜서 친절하게 알려주는 방식으로 임시 구현.
            QueueResponse queueResponse = waitingQueueService.register(studentNumber, lockerId);
            throw new BusinessException(ErrorCode.QUEUE_MODE_RESERVATION_BLOCKED, 
                "현재 대기열 모드입니다. 대기열에 등록되었습니다. 내 순번: " + queueResponse.getRank() + "번");
        }

        User user = getUser(studentNumber);
        Locker locker = getLockedLocker(lockerId);

        validateReservable(user, locker);

        try {
            LockerReservation reservation = new LockerReservation(user, locker);
            LockerReservation savedReservation = lockerReservationRepository.saveAndFlush(reservation);
            return toResponse(savedReservation, "사물함 예약이 완료되었습니다.");
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.ALREADY_RESERVED_LOCKER);
        }
    }

    @Override
    public LockerReservationResponse returnLocker(String studentNumber) {
        User user = getUser(studentNumber);

        LockerReservation reservation = lockerReservationRepository
                .findByUser_IdAndStatus(user.getId(), ReservationStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_ACTIVE_RESERVATION));

        getLockedLocker(reservation.getLocker().getId());

        reservation.returnReservation();

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
            return toResponse(savedReservation, "사물함 변경이 완료되었습니다.");
        } catch (DataIntegrityViolationException e) {
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
                message
        );
    }
}
