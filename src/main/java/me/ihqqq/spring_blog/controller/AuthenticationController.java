package me.ihqqq.spring_blog.controller;

import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.spring_blog.dto.request.*;
import me.ihqqq.spring_blog.dto.response.ApiResponse;
import me.ihqqq.spring_blog.dto.response.AuthenticationResponse;
import me.ihqqq.spring_blog.dto.response.IntrospectResponse;
import me.ihqqq.spring_blog.service.AuthenticationService;
import me.ihqqq.spring_blog.service.EmailVerificationService;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;
    EmailVerificationService emailVerificationService;

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request)
            throws JOSEException {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse>  introspect(@RequestBody IntrospectRequest request)
            throws JOSEException, ParseException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    /**
     * User nhận link trong email -> xác thực tài khoản
     */
    @GetMapping("/verify-email")
    ApiResponse<Void> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return ApiResponse.<Void>builder()
                .message("Email verified successfully, you can now log in")
                .build();
    }

    /**
     * Gửi lại email xác thực nếu link cũ hết hạn
     */
    @PostMapping("/resend-verification")
    ApiResponse<Void> resendVerificationEmail(@RequestBody @Valid ResendVerificationRequest request) {
        emailVerificationService.resendVerificationEmail(request.getEmail());
        return ApiResponse.<Void>builder()
                .message("Verification email has been resent. Please check your inbox.")
                .build();
    }

}
