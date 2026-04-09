package me.ihqqq.spring_blog.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.spring_blog.entity.EmailVerificationToken;
import me.ihqqq.spring_blog.entity.User;
import me.ihqqq.spring_blog.exception.AppException;
import me.ihqqq.spring_blog.exception.ErrorCode;
import me.ihqqq.spring_blog.repository.EmailVerificationTokenRepository;
import me.ihqqq.spring_blog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailVerificationService {

    EmailVerificationTokenRepository emailVerificationTokenRepository;
    UserRepository userRepository;
    EmailService emailService;

    static long TOKEN_EXPIRY_HOURS = 24;

    /**
     * Tạo token mới và gửi email sau khi đăng ký
     * Xóa token cũ nếu tồn tại (trường hợp resend)
     */

    @Transactional
    public void sendVerificationEmail(User user) {
        if(user.isEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        //Xóa token cũ nếu có
        emailVerificationTokenRepository.deleteByUserId(user.getId());

        String rawToken = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(rawToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
                .build();

        emailVerificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), rawToken);

        log.info("Verification email sent to user: {}", user.getUsername());

    }

    /**
     * Xác thực token từ link email
     */

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_VERIFICATION_TOKEN));

        if(verificationToken.isExpired()) {
            emailVerificationTokenRepository.delete(verificationToken);
            throw new AppException(ErrorCode.INVALID_VERIFICATION_TOKEN);
        }

        User user = verificationToken.getUser();
        if(user.isEmailVerified()) {
            emailVerificationTokenRepository.delete(verificationToken);
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        emailVerificationTokenRepository.delete(verificationToken);

        log.info("Email verified for user: {}", user.getUsername());
    }

    /**
     * Gửi lại email xác thực cho email address
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if(user.isEmailVerified()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_VERIFIED);
        }
        sendVerificationEmail(user);
    }

    /**
     * Xóa các token đã hết hạn (dùng cho scheduled job)
     */

    @Transactional
    public void cleanupExpiredTokens() {
        emailVerificationTokenRepository.deleteAllExpired(LocalDateTime.now());
        log.info("Expired verification tokens deleted");
    }

}
