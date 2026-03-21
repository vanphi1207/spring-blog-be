package me.ihqqq.spring_blog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AvatarSignatureResponse {
    String signature;
    String publicId;
    long timestamp;
    String apiKey;
    String cloudName;
}
