package me.ihqqq.spring_blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentUpdateRequest {
    @NotBlank(message = "COMMENT_CONTENT_REQUIRED")
    @Size(max = 2000, message = "COMMENT_CONTENT_TOO_LONG")
    String content;
}
