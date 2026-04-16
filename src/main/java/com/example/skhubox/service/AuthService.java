package com.example.skhubox.service;

import com.example.skhubox.domain.user.User;
import com.example.skhubox.dto.auth.LoginRequest;
import com.example.skhubox.dto.auth.LoginResponse;
import com.example.skhubox.dto.auth.SignupRequest;
import com.example.skhubox.dto.auth.EmailRequest;
import com.example.skhubox.dto.auth.EmailVerifyRequest;
import com.example.skhubox.exception.BusinessException;
import com.example.skhubox.exception.ErrorCode;
import com.example.skhubox.repository.UserRepository;
import com.example.skhubox.security.CustomUserDetails;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    private static final String EMAIL_VERIFY_KEY_PREFIX = "email:verify:";
    private static final String EMAIL_VERIFIED_KEY_PREFIX = "email:verified:";
    private static final long VERIFY_CODE_EXPIRATION = 5; // 5분
    private static final long VERIFIED_FLAG_EXPIRATION = 30; // 30분

    public void signup(SignupRequest request) {
        // 이메일 인증 여부 확인
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

        // 테스트용 관리자 계정 생성 로직 (학번이 999999999인 경우)
        if ("999999999".equals(request.getStudentNumber())) {
            user.assignAdminRole();
        }

        userRepository.save(user);
        
        // 가입 완료 후 인증 플래그 삭제
        redisTemplate.delete(EMAIL_VERIFIED_KEY_PREFIX + request.getEmail());
    }

    public void sendVerificationCode(EmailRequest request) {
        String email = request.getEmail();
        String code = generateCode();

        // Redis에 코드 저장 (5분간 유효)
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

        // 인증 성공 시 Redis에 인증 완료 플래그 저장 (30분간 유효)
        redisTemplate.opsForValue().set(
                EMAIL_VERIFIED_KEY_PREFIX + email,
                "true",
                VERIFIED_FLAG_EXPIRATION,
                TimeUnit.MINUTES
        );

        // 사용한 인증 코드는 삭제
        redisTemplate.delete(EMAIL_VERIFY_KEY_PREFIX + email);
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

            String token = jwtTokenProvider.createToken(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            return new LoginResponse(
                    token,
                    "Bearer",
                    userDetails.getUser().getRole().name()
            );
        } catch (BadCredentialsException e) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        } catch (AuthenticationException e) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
    }
}