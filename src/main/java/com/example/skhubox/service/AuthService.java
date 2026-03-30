package com.example.skhubox.service;

import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.auth.LoginRequest;
import com.example.skhubox.dto.auth.LoginResponse;
import com.example.skhubox.dto.auth.SignupRequest;
import com.example.skhubox.repository.UserRepository;
import com.example.skhubox.security.jwt.JwtTokenProvider;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void signup(SignupRequest request) {
        if (userRepository.findByStudentNumber(request.getStudentNumber()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 학번입니다.");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = new User(
                request.getStudentNumber(),
                request.getName(),
                request.getEmail(),
                request.getDepartment(),
                passwordEncoder.encode(request.getPassword())
        );

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request) {
        // 1. 학번 존재 여부 먼저 확인
        userRepository.findByStudentNumber(request.getStudentNumber())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        try {
            // 2. 비밀번호 인증 진행
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getStudentNumber(),
                            request.getPassword()
                    )
            );

            String token = jwtTokenProvider.createToken(authentication);
            return new LoginResponse(token, "Bearer");
        } catch (BadCredentialsException e) {
            // 학번은 존재하는데 인증에 실패한 경우이므로 비밀번호 오류임
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        } catch (DisabledException e) {
            throw new IllegalArgumentException("계정이 비활성화되었습니다.");
        } catch (LockedException e) {
            throw new IllegalArgumentException("계정이 잠겨 있습니다.");
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("로그인 인증에 실패했습니다.");
        }
    }
}