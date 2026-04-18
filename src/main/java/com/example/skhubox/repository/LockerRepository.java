package com.example.skhubox.repository;

import com.example.skhubox.domain.locker.Locker;
import com.example.skhubox.domain.locker.LockerStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LockerRepository extends JpaRepository<Locker, Long> {

    Optional<Locker> findByLockerNumber(String lockerNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Locker l where l.id = :lockerId")
    Optional<Locker> findByIdWithPessimisticLock(Long lockerId);

    long countByStatus(LockerStatus status);
}