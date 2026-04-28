package me.ihqqq.spring_blog.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.spring_blog.constant.PostStatus;
import me.ihqqq.spring_blog.dto.response.LikeResponse;
import me.ihqqq.spring_blog.entity.Post;
import me.ihqqq.spring_blog.entity.PostLike;
import me.ihqqq.spring_blog.entity.User;
import me.ihqqq.spring_blog.exception.AppException;
import me.ihqqq.spring_blog.exception.ErrorCode;
import me.ihqqq.spring_blog.repository.PostLikeRepository;
import me.ihqqq.spring_blog.repository.PostRepository;
import me.ihqqq.spring_blog.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostLikeService {

    PostLikeRepository postLikeRepository;
    PostRepository postRepository;
    UserRepository userRepository;

    /**
     * Toggle like/unlike một post.
     * Chỉ hoạt động với PUBLISHED posts.
     * Trả về trạng thái mới sau khi toggle.
     */
    @Transactional
    public LikeResponse toggleLike(String postId) {
        User currentUser = getCurrentUser();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        boolean alreadyLiked = postLikeRepository.existsByPostIdAndUserId(postId, currentUser.getId());

        if (alreadyLiked) {
            // Unlike
            postLikeRepository.deleteByPostIdAndUserId(postId, currentUser.getId());
            log.info("User {} unliked post {}", currentUser.getUsername(), postId);
        } else {
            // Like
            PostLike like = PostLike.builder()
                    .post(post)
                    .user(currentUser)
                    .build();
            postLikeRepository.save(like);
            log.info("User {} liked post {}", currentUser.getUsername(), postId);
        }

        long likeCount = postLikeRepository.countByPostId(postId);

        return LikeResponse.builder()
                .postId(postId)
                .likeCount(likeCount)
                .liked(!alreadyLiked)
                .build();
    }

    /**
     * Lấy thông tin like của một post.
     * Nếu chưa đăng nhập thì liked = false.
     */
    @Transactional(readOnly = true)
    public LikeResponse getLikeInfo(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        long likeCount = postLikeRepository.countByPostId(postId);

        // Kiểm tra xem current user đã like chưa (nếu đã đăng nhập)
        boolean liked = false;
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if (username != null && !username.equals("anonymousUser")) {
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    liked = postLikeRepository.existsByPostIdAndUserId(postId, user.getId());
                }
            }
        } catch (Exception ignored) {
        }

        return LikeResponse.builder()
                .postId(postId)
                .likeCount(likeCount)
                .liked(liked)
                .build();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}