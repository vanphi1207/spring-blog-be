package me.ihqqq.spring_blog.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.spring_blog.constant.PostStatus;
import me.ihqqq.spring_blog.dto.request.CommentRequest;
import me.ihqqq.spring_blog.dto.request.CommentUpdateRequest;
import me.ihqqq.spring_blog.dto.response.CommentResponse;
import me.ihqqq.spring_blog.entity.Comment;
import me.ihqqq.spring_blog.entity.Post;
import me.ihqqq.spring_blog.entity.User;
import me.ihqqq.spring_blog.exception.AppException;
import me.ihqqq.spring_blog.exception.ErrorCode;
import me.ihqqq.spring_blog.mapper.CommentMapper;
import me.ihqqq.spring_blog.repository.CommentRepository;
import me.ihqqq.spring_blog.repository.PostRepository;
import me.ihqqq.spring_blog.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommentService {

    CommentRepository commentRepository;
    PostRepository postRepository;
    UserRepository userRepository;
    CommentMapper commentMapper;


    /**
     * Lấy danh sách top-level comments của bài post (đã published).
     * Kèm theo replies cho mỗi comment.
     */
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPost(String postId, Pageable pageable) {
        // Kiểm tra bài post tồn tại và đã published
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        Page<Comment> topLevel = commentRepository.findTopLevelByPostId(postId, pageable);

        return topLevel.map(comment -> {
            CommentResponse response = commentMapper.toCommentResponse(comment);

            // Load replies (1 cấp) - tránh hiển thị content của deleted reply
            List<CommentResponse> replies = commentRepository
                    .findRepliesByParentId(comment.getId())
                    .stream()
                    .map(reply -> {
                        CommentResponse replyResponse = commentMapper.toCommentResponse(reply);
                        if (reply.isDeleted()) {
                            replyResponse.setContent("[Bình luận đã bị xoá]");
                        }
                        return replyResponse;
                    })
                    .collect(Collectors.toList());

            response.setReplies(replies);

            // Ẩn nội dung nếu bị soft-deleted
            if (comment.isDeleted()) {
                response.setContent("[Bình luận đã bị xoá]");
            }

            return response;
        });
    }

    /**
     * Tạo comment mới hoặc reply cho một comment.
     * Chỉ cho phép comment trên bài PUBLISHED.
     * Reply chỉ được phép ở cấp 1 (không reply lồng nhau).
     */
    @Transactional
    public CommentResponse createComment(String postId, CommentRequest request) {
        User author = getCurrentUser();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new AppException(ErrorCode.CANNOT_COMMENT_ON_UNPUBLISHED_POST);
        }

        Comment.CommentBuilder builder = Comment.builder()
                .content(request.getContent())
                .post(post)
                .author(author);

        // Xử lý reply
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

            // Chỉ cho phép reply cấp 1 (parent phải là top-level)
            if (parent.getParent() != null) {
                throw new AppException(ErrorCode.NESTED_REPLY_NOT_ALLOWED);
            }

            // Parent phải thuộc cùng bài post
            if (!parent.getPost().getId().equals(postId)) {
                throw new AppException(ErrorCode.COMMENT_NOT_FOUND);
            }

            if (parent.isDeleted()) {
                throw new AppException(ErrorCode.COMMENT_NOT_FOUND);
            }

            builder.parent(parent);
        }

        Comment saved = commentRepository.save(builder.build());
        log.info("Comment created: {} by user: {}", saved.getId(), author.getUsername());

        CommentResponse response = commentMapper.toCommentResponse(saved);
        response.setReplies(List.of());
        return response;
    }

    /**
     * Cập nhật nội dung comment.
     * Chỉ chủ comment được sửa.
     */
    @Transactional
    public CommentResponse updateComment(String commentId, CommentUpdateRequest request) {
        Comment comment = getCommentAndCheckOwnership(commentId);

        comment.setContent(request.getContent());
        Comment saved = commentRepository.save(comment);

        log.info("Comment updated: {}", commentId);
        CommentResponse response = commentMapper.toCommentResponse(saved);
        response.setReplies(List.of());
        return response;
    }

    /**
     * Xoá mềm comment.
     * - Chủ comment: soft delete
     * - Admin: có thể hard delete (dùng endpoint riêng)
     */
    @Transactional
    public void deleteComment(String commentId) {
        Comment comment = getCommentAndCheckOwnership(commentId);
        softDelete(comment);
        log.info("Comment soft-deleted: {} by user: {}", commentId,
                SecurityContextHolder.getContext().getAuthentication().getName());
    }

    /**
     * Admin: Hard delete comment (xoá hẳn khỏi DB).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void adminDeleteComment(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        commentRepository.delete(comment);
        log.warn("Comment hard-deleted by admin: {}", commentId);
    }

    /**
     * Admin: Lấy tất cả comment của bài post, kể cả đã xoá.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<CommentResponse> adminGetCommentsByPost(String postId, Pageable pageable) {
        if (!postRepository.existsById(postId)) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }
        return commentRepository.findByPostId(postId, pageable)
                .map(commentMapper::toCommentResponse);
    }

    /**
     * Lấy số lượng comment của một bài post.
     */
    @Transactional(readOnly = true)
    public long countCommentsByPost(String postId) {
        if (!postRepository.existsById(postId)) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }
        return commentRepository.countByPostId(postId);
    }

    private void softDelete(Comment comment) {
        comment.setDeleted(true);
        // Soft delete cả replies nếu là top-level comment
        if (comment.getParent() == null && !comment.getReplies().isEmpty()) {
            comment.getReplies().forEach(reply -> reply.setDeleted(true));
        }
        commentRepository.save(comment);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * Lấy comment và kiểm tra quyền sở hữu (chủ comment hoặc admin).
     */
    private Comment getCommentAndCheckOwnership(String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.isDeleted()) {
            throw new AppException(ErrorCode.COMMENT_NOT_FOUND);
        }

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !comment.getAuthor().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return comment;
    }
}
