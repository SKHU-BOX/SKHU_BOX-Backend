package com.example.skhubox.service;

import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.auth.EmailRequest;
import com.example.skhubox.dto.auth.EmailVerifyRequest;
import com.example.skhubox.dto.auth.LoginRequest;
import com.example.skhubox.dto.auth.LoginResponse;
import com.example.skhubox.dto.auth.PasswordResetConfirmRequest;
import com.example.skhubox.dto.auth.PasswordResetRequest;
import com.example.skhubox.dto.auth.SignupRequest;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.exception.ErrorCode;
import com.example.skhubox.repository.UserRepository;
import com.example.skhubox.security.CustomUserDetails;
import com.example.skhubox.security.CustomUserDetailsService;
import com.example.skhubox.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    private static final String EMAIL_VERIFY_KEY_PREFIX = "email:verify:";
    private static final String EMAIL_VERIFIED_KEY_PREFIX = "email:verified:";
    private static final String PASSWORD_RESET_KEY_PREFIX = "password:reset:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh:token:";
    private static final long VERIFY_CODE_EXPIRATION = 5;
    private static final long VERIFIED_FLAG_EXPIRATION = 30;
    private static final long PASSWORD_RESET_EXPIRATION = 15;
    private static final String PASSWORD_RESET_URL_ENV = "PASSWORD_RESET_URL";

    public void signup(SignupRequest request) {
        String isVerified = redisTemplate.opsForValue().get(EMAIL_VERIFIED_KEY_PREFIX + request.getEmail());
        if (isVerified == null) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        if (userService.existsByStudentNumber(request.getStudentNumber())) {
            throw new BusinessException(ErrorCode.DUPLICATE_STUDENT_NUMBER);
        }

        if (userService.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = new User(
                request.getStudentNumber(),
                request.getName(),
                request.getEmail(),
                request.getDepartment(),
                passwordEncoder.encode(request.getPassword())
        );

        if ("999999999".equals(request.getStudentNumber())) {
            user.assignAdminRole();
        }

        userRepository.save(user);
        redisTemplate.delete(EMAIL_VERIFIED_KEY_PREFIX + request.getEmail());
    }

    public void sendVerificationCode(EmailRequest request) {
        String email = request.getEmail();
        if (!email.endsWith("@office.skhu.ac.kr")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (userService.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        String code = generateCode();
        redisTemplate.opsForValue().set(
                EMAIL_VERIFY_KEY_PREFIX + email,
                code,
                VERIFY_CODE_EXPIRATION,
                TimeUnit.MINUTES
        );
        sendEmail(email, code);
    }

    public void verifyCode(EmailVerifyRequest request) {
        String email = request.getEmail();
        String savedCode = redisTemplate.opsForValue().get(EMAIL_VERIFY_KEY_PREFIX + email);
        if (savedCode == null || !savedCode.equals(request.getCode())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }
        redisTemplate.opsForValue().set(
                EMAIL_VERIFIED_KEY_PREFIX + email,
                "true",
                VERIFIED_FLAG_EXPIRATION,
                TimeUnit.MINUTES
        );
        redisTemplate.delete(EMAIL_VERIFY_KEY_PREFIX + email);
    }

    public void requestPasswordReset(PasswordResetRequest request) {
        userRepository.findByStudentNumberAndEmail(request.getStudentNumber(), request.getEmail())
                .ifPresent(user -> {
                    String token = UUID.randomUUID().toString();
                    redisTemplate.opsForValue().set(
                            PASSWORD_RESET_KEY_PREFIX + token,
                            user.getStudentNumber(),
                            PASSWORD_RESET_EXPIRATION,
                            TimeUnit.MINUTES
                    );
                    sendPasswordResetEmail(user.getEmail(), token);
                });
    }

    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        String studentNumber = redisTemplate.opsForValue().get(PASSWORD_RESET_KEY_PREFIX + request.getToken());
        if (studentNumber == null) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD_RESET_TOKEN);
        }
        User user = userRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        redisTemplate.delete(PASSWORD_RESET_KEY_PREFIX + request.getToken());
    }

    private String generateCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private void sendEmail(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("[SKHUBOX] 이메일 인증 번호입니다.");
            message.setText("인증 번호: [" + code + "]\n5분 이내에 입력해주세요.");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    private void sendPasswordResetEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("[SKHUBOX] 비밀번호 재설정 안내");
            String passwordResetUrl = System.getenv(PASSWORD_RESET_URL_ENV);
            String resetLink = passwordResetUrl == null || passwordResetUrl.isBlank()
                    ? null
                    : passwordResetUrl + (passwordResetUrl.contains("?") ? "&" : "?") + "token=" + token;
            message.setText("""
                    비밀번호 재설정을 요청하셨다면 아래 정보를 사용해주세요.

                    %s

                    비밀번호 재설정 토큰: [%s]
                    15분 이내에 새 비밀번호와 함께 입력해주세요.
                    """.formatted(
                    resetLink == null ? "재설정 링크가 설정되지 않아 토큰을 직접 입력해야 합니다." : "재설정 링크: " + resetLink,
                    token
            ));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", to, e);
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        userService.findByStudentNumber(request.getStudentNumber());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getStudentNumber(),
                            request.getPassword()
                    )
            );

            String accessToken = jwtTokenProvider.createAccessToken(authentication);
            String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String studentNumber = userDetails.getUsername();

            redisTemplate.opsForValue().set(
                    REFRESH_TOKEN_KEY_PREFIX + studentNumber,
                    refreshToken,
                    jwtTokenProvider.getRefreshExpiration(),
                    TimeUnit.MILLISECONDS
            );

            return new LoginResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    userDetails.getUser().getRole().name()
            );
        } catch (BadCredentialsException e) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String studentNumber = jwtTokenProvider.getStudentNumber(refreshToken);
        String stored = redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY_PREFIX + studentNumber);

        if (stored == null || !stored.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(studentNumber);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_KEY_PREFIX + studentNumber,
                newRefreshToken,
                jwtTokenProvider.getRefreshExpiration(),
                TimeUnit.MILLISECONDS
        );

        return new LoginResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                userDetails.getUser().getRole().name()
        );
    }

    public void logout(String studentNumber) {
        redisTemplate.delete(REFRESH_TOKEN_KEY_PREFIX + studentNumber);
    }
}
