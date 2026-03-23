package me.ihqqq.spring_blog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ThumbnailSignatureResponse {
    String signature;
    String publicId;
    long timestamp;
    String apiKey;
    String cloudName;
}
