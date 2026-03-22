package me.ihqqq.spring_blog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import me.ihqqq.spring_blog.constant.PostStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    String id;
    String title;
    String slug;
    String excerpt;
    String content;
    String thumbnailUrl;
    PostStatus status;
    long viewCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    AuthorResponse author;
}
