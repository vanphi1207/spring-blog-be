package me.ihqqq.spring_blog.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.spring_blog.dto.response.ApiResponse;
import me.ihqqq.spring_blog.dto.response.LikeResponse;
import me.ihqqq.spring_blog.service.PostLikeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostLikeController {

    PostLikeService postLikeService;

    /**
     * Lấy số like và trạng thái like của current user
     */
    @GetMapping("/{postId}/likes")
    ApiResponse<LikeResponse> getLikeInfo(@PathVariable String postId) {
        return ApiResponse.<LikeResponse>builder()
                .result(postLikeService.getLikeInfo(postId))
                .build();
    }

    /**
     * Toggle like/unlike - yêu cầu đăng nhập
     */
    @PostMapping("/{postId}/likes")
    ApiResponse<LikeResponse> toggleLike(@PathVariable String postId) {
        return ApiResponse.<LikeResponse>builder()
                .result(postLikeService.toggleLike(postId))
                .build();
    }
}
