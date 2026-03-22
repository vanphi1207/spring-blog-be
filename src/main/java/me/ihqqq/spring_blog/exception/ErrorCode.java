package me.ihqqq.spring_blog.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User already existed", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1003, "User not found", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1004, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1005, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED(1009, "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_TOO_LARGE(1010, "File size must be not exceed 5MB", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE(1011, "Only JPEG, PNG and WebP images are allowed", HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND(1012, "Post not found", HttpStatus.NOT_FOUND),
    TITLE_REQUIRED(1013, "Title is required", HttpStatus.BAD_REQUEST),
    TITLE_TOO_LONG(1021, "Title must not exceed 255 characters", HttpStatus.BAD_REQUEST),
    CONTENT_REQUIRED(1022, "Content is required", HttpStatus.BAD_REQUEST),
    EXCERPT_TOO_LONG(1023, "Excerpt must not exceed 500 characters", HttpStatus.BAD_REQUEST),
    ;


    int code;
    String message;
    HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
