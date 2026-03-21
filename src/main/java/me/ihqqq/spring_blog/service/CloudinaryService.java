package me.ihqqq.spring_blog.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.ihqqq.spring_blog.dto.response.AvatarSignatureResponse;
import me.ihqqq.spring_blog.exception.AppException;
import me.ihqqq.spring_blog.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CloudinaryService {

    final Cloudinary cloudinary;

    @Value("${cloudinary.api-key}")
    String apiKey;

    @Value("${cloudinary.cloud-name}")
    String cloudName;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    // Tạo signature để client upload thẳng lên Cloudinary
    public AvatarSignatureResponse generateUploadSignature() {
        try {
            String publicId = "avatars/" + UUID.randomUUID();
            long timestamp = System.currentTimeMillis() / 1000L;

            // Các params phải sort theo alphabet khi ký
            Map<String, Object> params = ObjectUtils.asMap(
                    "public_id",      publicId,
                    "timestamp",      timestamp,
                    "transformation", "c_fill,g_face,w_400,h_400,q_auto,f_auto"
            );

            String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret);

            return AvatarSignatureResponse.builder()
                    .signature(signature)
                    .publicId(publicId)
                    .timestamp(timestamp)
                    .apiKey(apiKey)
                    .cloudName(cloudName)
                    .build();

        } catch (Exception e) {
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }
    }
}