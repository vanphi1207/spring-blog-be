package me.ihqqq.spring_blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.spring_blog.constant.PostStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostRequest {

    @NotBlank(message = "TITLE_REQUIRED")
    @Size(max = 255, message = "TITLE_TOO_LONG")
    String title;

    @Size(max = 500, message = "EXCERPT_TOO_LONG")
    String excerpt;

    @NotBlank(message = "CONTENT_REQUIRED")
    String content;

    String thumbnailUrl;

    PostStatus status;
}
