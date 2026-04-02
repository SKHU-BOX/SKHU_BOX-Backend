package com.example.skhubox.service;

import com.example.skhubox.domain.locker.Locker;
import com.example.skhubox.domain.reservation.LockerReservation;
import com.example.skhubox.domain.reservation.ReservationStatus;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.LockerReservationResponse;
import com.example.skhubox.dto.LockerResponse;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.exception.ErrorCode;
import com.example.skhubox.repository.LockerRepository;
import com.example.skhubox.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.skhubox.repository.LockerReservationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LockerReservationServiceImpl implements LockerReservationService {

    private final UserRepository userRepository;
    private final LockerRepository lockerRepository;
    private final LockerReservationRepository lockerReservationRepository;

    public LockerReservationServiceImpl(UserRepository userRepository,
                                        LockerRepository lockerRepository,
                                        LockerReservationRepository lockerReservationRepository) {
        this.userRepository = userRepository;
        this.lockerRepository = lockerRepository;
        this.lockerReservationRepository = lockerReservationRepository;
    }

    @Override
    public LockerReservationResponse reserveLocker(String studentNumber, Long lockerId) {
        User user = getUser(studentNumber);
        Locker locker = getLockedLocker(lockerId);

        validateReservable(user, locker);

        LockerReservation reservation = new LockerReservation(user, locker);
        LockerReservation savedReservation = lockerReservationRepository.save(reservation);

        return toResponse(savedReservation, "사물함 예약이 완료되었습니다.");
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

        currentReservation.returnReservation();

        LockerReservation newReservation = new LockerReservation(user, newLocker);
        LockerReservation savedReservation = lockerReservationRepository.save(newReservation);

        return toResponse(savedReservation, "사물함 변경이 완료되었습니다.");
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
