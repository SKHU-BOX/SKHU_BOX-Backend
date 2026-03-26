package com.example.skhubox.service;

import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.auth.AuthResponse;
import com.example.skhubox.dto.auth.LoginRequest;
import com.example.skhubox.dto.auth.SignupRequest;
import com.example.skhubox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new IllegalArgumentException("이미 존재하는 학번입니다.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = new User(
                request.getStudentNumber(),
                request.getName(),
                request.getEmail(),
                request.getDepartment(),
                passwordEncoder.encode(request.getPassword())
        );

        User savedUser = userRepository.save(user);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getStudentNumber(),
                savedUser.getName(),
                "회원가입 성공"
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByStudentNumber(request.getStudentNumber())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학번입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return new AuthResponse(
                user.getId(),
                user.getStudentNumber(),
                user.getName(),
                "로그인 성공"
        );
    }
}