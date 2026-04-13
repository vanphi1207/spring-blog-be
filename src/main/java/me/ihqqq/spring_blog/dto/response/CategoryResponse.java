package me.ihqqq.spring_blog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    String id;
    String name;
    String slug;
    String description;
    long postCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
