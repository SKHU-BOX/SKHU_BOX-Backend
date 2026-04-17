package com.example.skhubox.repository;

import com.example.skhubox.domain.reservation.LockerReservation;
import com.example.skhubox.domain.reservation.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LockerReservationRepository extends JpaRepository<LockerReservation, Long> {

    boolean existsByUser_IdAndStatus(Long userId, ReservationStatus status);

    boolean existsByLocker_IdAndStatus(Long lockerId, ReservationStatus status);

    Optional<LockerReservation> findByUser_IdAndStatus(Long userId, ReservationStatus status);

    Optional<LockerReservation> findByLocker_IdAndStatus(Long lockerId, ReservationStatus status);

    List<LockerReservation> findAllByStatus(ReservationStatus status);

    long countByLocker_IdAndStatus(Long lockerId, ReservationStatus status);
}