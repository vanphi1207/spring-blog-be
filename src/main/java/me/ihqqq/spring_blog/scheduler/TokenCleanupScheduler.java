package me.ihqqq.spring_blog.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.spring_blog.service.EmailVerificationService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final EmailVerificationService emailVerificationService;

    /**
     * Chạy lúc 2:00 sáng mỗi ngày để xóa các token đã hết hạn
     * Cron: giây phút giờ ngày tháng thứ
     */
    public void cleanupExpiredVerificationTokens() {
        log.info("Running scheduled cleanup of expired email verification tokens....");
        emailVerificationService.cleanupExpiredTokens();
    }

}
