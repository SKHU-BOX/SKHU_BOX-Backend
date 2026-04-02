package com.example.skhubox.service;

import com.example.skhubox.domain.locker.Locker;
import com.example.skhubox.domain.reservation.LockerReservation;
import com.example.skhubox.domain.reservation.ReservationStatus;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.LockerReservationResponse;
import com.example.skhubox.dto.LockerResponse;
import com.example.skhubox.exception.BusinessException;
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

        // 예약 가능 여부 검증 분리
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
                .orElseThrow(() -> new BusinessException("현재 이용 중인 사물함이 없습니다."));

        // 반납 시 사물함 락 획득 (경쟁 상태 방어)
        getLockedLocker(reservation.getLocker().getId());

        reservation.returnReservation();

        return toResponse(reservation, "사물함 반납이 완료되었습니다.");
    }

    @Override
    public LockerReservationResponse changeLocker(String studentNumber, Long newLockerId) {
        User user = getUser(studentNumber);

        LockerReservation currentReservation = lockerReservationRepository
                .findByUser_IdAndStatus(user.getId(), ReservationStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("변경할 기존 예약 내역이 없습니다."));

        Long currentLockerId = currentReservation.getLocker().getId();

        if (currentLockerId.equals(newLockerId)) {
            throw new BusinessException("동일한 사물함으로 변경할 수 없습니다.");
        }

        // [데드락 방지] 사물함 ID 순서대로 락 획득
        Long firstId = Math.min(currentLockerId, newLockerId);
        Long secondId = Math.max(currentLockerId, newLockerId);

        getLockedLocker(firstId);
        Locker secondLocker = getLockedLocker(secondId);

        Locker newLocker = (firstId.equals(newLockerId)) ? getLockedLocker(firstId) : secondLocker;

        // 새 사물함 검증
        validateNewLocker(newLocker);

        // 기존 예약 반납 및 새 예약 생성
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
                .orElseThrow(() -> new BusinessException("현재 이용 중인 사물함이 없습니다."));

        return toResponse(reservation, "현재 예약 정보 조회 성공");
    }

    // --- Private Helper Methods (리팩토링의 핵심) ---

    private User getUser(String studentNumber) {
        return userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException("존재하지 않는 사용자입니다."));
    }

    private Locker getLockedLocker(Long lockerId) {
        return lockerRepository.findByIdWithPessimisticLock(lockerId)
                .orElseThrow(() -> new BusinessException("사물함 정보가 존재하지 않습니다."));
    }

    private void validateReservable(User user, Locker locker) {
        if (!locker.isNormal()) {
            throw new BusinessException("해당 사물함은 현재 사용 불가 상태입니다.");
        }

        if (lockerReservationRepository.existsByUser_IdAndStatus(user.getId(), ReservationStatus.ACTIVE)) {
            throw new BusinessException("이미 이용 중인 사물함이 있습니다.");
        }

        if (lockerReservationRepository.existsByLocker_IdAndStatus(locker.getId(), ReservationStatus.ACTIVE)) {
            throw new BusinessException("이미 다른 사용자가 이용 중인 사물함입니다.");
        }
    }

    private void validateNewLocker(Locker newLocker) {
        if (!newLocker.isNormal()) {
            throw new BusinessException("변경하려는 사물함은 현재 사용 불가 상태입니다.");
        }

        if (lockerReservationRepository.existsByLocker_IdAndStatus(newLocker.getId(), ReservationStatus.ACTIVE)) {
            throw new BusinessException("변경하려는 사물함은 이미 다른 사용자가 이용 중입니다.");
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
