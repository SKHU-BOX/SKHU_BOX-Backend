package com.example.skhubox.repository;

import com.example.skhubox.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByStudentNumber(String studentNumber);

    Optional<User> findByEmail(String email);

    boolean existsByStudentNumber(String studentNumber);

    boolean existsByEmail(String email);
}