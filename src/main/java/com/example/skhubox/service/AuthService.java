package com.example.skhubox.service;

import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.auth.LoginRequest;
import com.example.skhubox.dto.auth.LoginResponse;
import com.example.skhubox.dto.auth.SignupRequest;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.repository.UserRepository;
import com.example.skhubox.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public void signup(SignupRequest request) {
        if (userService.existsByStudentNumber(request.getStudentNumber())) {
            throw new BusinessException("이미 존재하는 학번입니다.");
        }

        if (userService.existsByEmail(request.getEmail())) {
            throw new BusinessException("이미 존재하는 이메일입니다.");
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

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 유저 존재 여부 확인
        userService.findByStudentNumber(request.getStudentNumber());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getStudentNumber(),
                            request.getPassword()
                    )
            );

            String token = jwtTokenProvider.createToken(authentication);
            return new LoginResponse(token, "Bearer");
        } catch (BadCredentialsException e) {
            throw new BusinessException("비밀번호가 일치하지 않습니다.");
        } catch (AuthenticationException e) {
            throw new BusinessException("로그인 인증에 실패했습니다.");
        }
    }
}
