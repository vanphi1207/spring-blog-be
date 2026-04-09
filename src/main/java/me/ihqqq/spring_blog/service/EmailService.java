package me.ihqqq.spring_blog.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.spring_blog.exception.AppException;
import me.ihqqq.spring_blog.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailService {

    JavaMailSender mailSender;

    @NonFinal
    @Value("${app.mail.from}")
    String fromEmail;

    @NonFinal
    @Value("${app.base-url}")
    String baseUrl;

    @Async
    public void sendVerificationEmail(String toEmail, String username, String token) {
        String verifyUrl = baseUrl + "/api/v1/auth/verify-email?token=" + token;

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8"/>
                  <style>
                    body { font-family: sans-serif; background: #f5f5f5; margin: 0; padding: 0; }
                    .container { max-width: 560px; margin: 40px auto; background: #fff;
                                 border-radius: 10px; padding: 40px; }
                    .btn { display: inline-block; background: #4F46E5; color: #fff !important;
                           padding: 12px 28px; border-radius: 6px; text-decoration: none;
                           font-weight: 600; margin: 24px 0; }
                    .footer { color: #888; font-size: 12px; margin-top: 32px; }
                  </style>
                </head>
                <body>
                  <div class="container">
                    <h2>Xác thực email của bạn</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Cảm ơn bạn đã đăng ký tài khoản. Vui lòng nhấn vào nút bên dưới
                       để xác thực địa chỉ email của bạn:</p>
                    <a class="btn" href="%s">Xác thực email</a>
                    <p>Hoặc copy link sau vào trình duyệt:</p>
                    <p style="word-break:break-all; color:#555;">%s</p>
                    <p>Link có hiệu lực trong <strong>24 giờ</strong>.</p>
                    <div class="footer">
                      Nếu bạn không đăng ký tài khoản này, hãy bỏ qua email này.
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(username, verifyUrl, verifyUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Xác thực email – Spring Blog");
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
