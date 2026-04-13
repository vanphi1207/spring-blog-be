package me.ihqqq.spring_blog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TagResponse {
    String id;
    String name;
    String slug;
    long postCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}