package com.example.skhubox.service;

import com.example.skhubox.domain.locker.Locker;
import com.example.skhubox.domain.reservation.LockerReservation;
import com.example.skhubox.domain.reservation.ReservationStatus;
import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.LockerReservationResponse;
import com.example.skhubox.dto.LockerResponse;
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
        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 비관적 락으로 사물함 점유
        Locker locker = lockerRepository.findByIdWithPessimisticLock(lockerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사물함입니다."));

        if (!locker.isNormal()) {
            throw new IllegalArgumentException("해당 사물함은 현재 사용 불가 상태입니다.");
        }

        if (lockerReservationRepository.existsByUser_IdAndStatus(user.getId(), ReservationStatus.ACTIVE)) {
            throw new IllegalArgumentException("이미 이용 중인 사물함이 있습니다.");
        }

        if (lockerReservationRepository.existsByLocker_IdAndStatus(lockerId, ReservationStatus.ACTIVE)) {
            throw new IllegalArgumentException("이미 다른 사용자가 이용 중인 사물함입니다.");
        }

        LockerReservation reservation = new LockerReservation(user, locker);
        LockerReservation savedReservation = lockerReservationRepository.save(reservation);

        return new LockerReservationResponse(
                savedReservation.getId(),
                user.getId(),
                locker.getId(),
                savedReservation.getStatus().name(),
                "사물함 예약이 완료되었습니다."
        );
    }

    @Override
    public LockerReservationResponse returnLocker(String studentNumber) {
        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        LockerReservation reservation = lockerReservationRepository
                .findByUser_IdAndStatus(user.getId(), ReservationStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("사용 중인 사물함이 없습니다."));

        // 반납 시에도 사물함 락을 잡아 changeLocker 등과의 경쟁 상태 방어
        lockerRepository.findByIdWithPessimisticLock(reservation.getLocker().getId())
                .orElseThrow(() -> new IllegalArgumentException("사물함 정보가 존재하지 않습니다."));

        reservation.returnReservation();

        return new LockerReservationResponse(
                reservation.getId(),
                user.getId(),
                reservation.getLocker().getId(),
                reservation.getStatus().name(),
                "사물함 반납이 완료되었습니다."
        );
    }

    @Override
    public LockerReservationResponse changeLocker(String studentNumber, Long newLockerId) {
        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        LockerReservation currentReservation = lockerReservationRepository
                .findByUser_IdAndStatus(user.getId(), ReservationStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("현재 사용 중인 사물함이 없습니다."));

        Long currentLockerId = currentReservation.getLocker().getId();

        if (currentLockerId.equals(newLockerId)) {
            throw new IllegalArgumentException("동일한 사물함으로 변경할 수 없습니다.");
        }

        // [데드락 방지] 사물함 ID 순서대로 락을 획득하여 순환 대기 방지
        Long firstId = Math.min(currentLockerId, newLockerId);
        Long secondId = Math.max(currentLockerId, newLockerId);

        Locker firstLocker = lockerRepository.findByIdWithPessimisticLock(firstId)
                .orElseThrow(() -> new IllegalArgumentException("사물함 정보가 존재하지 않습니다."));
        Locker secondLocker = lockerRepository.findByIdWithPessimisticLock(secondId)
                .orElseThrow(() -> new IllegalArgumentException("사물함 정보가 존재하지 않습니다."));

        Locker newLocker = (firstId.equals(newLockerId)) ? firstLocker : secondLocker;

        // 새 사물함 검증
        if (!newLocker.isNormal()) {
            throw new IllegalArgumentException("새 사물함은 현재 사용 불가 상태입니다.");
        }

        if (lockerReservationRepository.existsByLocker_IdAndStatus(newLockerId, ReservationStatus.ACTIVE)) {
            throw new IllegalArgumentException("새 사물함은 이미 다른 사용자가 사용 중입니다.");
        }

        // 기존 예약 반납 및 새 예약 생성
        currentReservation.returnReservation();

        LockerReservation newReservation = new LockerReservation(user, newLocker);
        LockerReservation savedReservation = lockerReservationRepository.save(newReservation);

        return new LockerReservationResponse(
                savedReservation.getId(),
                user.getId(),
                newLocker.getId(),
                savedReservation.getStatus().name(),
                "사물함 변경이 완료되었습니다."
        );
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
        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        LockerReservation reservation = lockerReservationRepository
                .findByUser_IdAndStatus(user.getId(), ReservationStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("현재 사용 중인 사물함이 없습니다."));

        return new LockerReservationResponse(
                reservation.getId(),
                user.getId(),
                reservation.getLocker().getId(),
                reservation.getStatus().name(),
                "현재 예약 정보 조회 성공"
        );
    }
}
