package com.example.skhubox.repository;

import com.example.skhubox.domain.reservation.LockerReservation;
import com.example.skhubox.domain.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LockerReservationRepository extends JpaRepository<LockerReservation, Long> {

    boolean existsByUser_IdAndStatus(Long userId, ReservationStatus status);

    boolean existsByUser_IdAndStatusAndExpiredAtAfter(Long userId, ReservationStatus status, LocalDateTime expiredAt);

    boolean existsByLocker_IdAndStatus(Long lockerId, ReservationStatus status);

    boolean existsByLocker_IdAndStatusAndExpiredAtAfter(Long lockerId, ReservationStatus status, LocalDateTime expiredAt);

    Optional<LockerReservation> findByUser_IdAndStatus(Long userId, ReservationStatus status);

    Optional<LockerReservation> findByUser_IdAndStatusAndExpiredAtAfter(Long userId, ReservationStatus status, LocalDateTime expiredAt);

    Optional<LockerReservation> findByLocker_IdAndStatus(Long lockerId, ReservationStatus status);

    List<LockerReservation> findAllByStatus(ReservationStatus status);

    List<LockerReservation> findAllByStatusAndExpiredAtBefore(ReservationStatus status, LocalDateTime expiredAt);

    long countByLocker_IdAndStatus(Long lockerId, ReservationStatus status);
}
