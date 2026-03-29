package me.ihqqq.spring_blog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {

    String id;
    String content;
    boolean deleted;
    String postId;
    String parentId;
    AuthorResponse author;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    List<CommentResponse> replies;
}
