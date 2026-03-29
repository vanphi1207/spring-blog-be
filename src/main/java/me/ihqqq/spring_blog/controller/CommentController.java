package me.ihqqq.spring_blog.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.spring_blog.dto.request.CommentRequest;
import me.ihqqq.spring_blog.dto.request.CommentUpdateRequest;
import me.ihqqq.spring_blog.dto.response.ApiResponse;
import me.ihqqq.spring_blog.dto.response.CommentResponse;
import me.ihqqq.spring_blog.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {

    CommentService commentService;


    /**
     * GET /posts/{postId}/comments
     * Lấy danh sách comment (có replies) của bài post đã published.
     */
    @GetMapping("/posts/{postId}/comments")
    ApiResponse<Page<CommentResponse>> getComments(
            @PathVariable String postId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.<Page<CommentResponse>>builder()
                .result(commentService.getCommentsByPost(postId, pageable))
                .build();
    }

    /**
     * GET /posts/{postId}/comments/count
     * Lấy số lượng comment của bài post.
     */
    @GetMapping("/posts/{postId}/comments/count")
    ApiResponse<Long> countComments(@PathVariable String postId) {
        return ApiResponse.<Long>builder()
                .result(commentService.countCommentsByPost(postId))
                .build();
    }


    /**
     * POST /posts/{postId}/comments
     * Tạo comment mới hoặc reply (nếu body có parentId).
     */
    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<CommentResponse> createComment(
            @PathVariable String postId,
            @RequestBody @Valid CommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.createComment(postId, request))
                .build();
    }

    /**
     * PUT /comments/{commentId}
     * Cập nhật nội dung comment (chỉ chủ comment).
     */
    @PutMapping("/comments/{commentId}")
    ApiResponse<CommentResponse> updateComment(
            @PathVariable String commentId,
            @RequestBody @Valid CommentUpdateRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.updateComment(commentId, request))
                .build();
    }

    /**
     * DELETE /comments/{commentId}
     * Soft delete comment (chủ comment hoặc admin).
     */
    @DeleteMapping("/comments/{commentId}")
    ApiResponse<Void> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(commentId);
        return ApiResponse.<Void>builder().build();
    }


    /**
     * GET /admin/posts/{postId}/comments
     * Admin xem tất cả comment kể cả đã xoá.
     */
    @GetMapping("/admin/posts/{postId}/comments")
    ApiResponse<Page<CommentResponse>> adminGetComments(
            @PathVariable String postId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<Page<CommentResponse>>builder()
                .result(commentService.adminGetCommentsByPost(postId, pageable))
                .build();
    }

    /**
     * DELETE /admin/comments/{commentId}
     * Admin hard delete comment.
     */
    @DeleteMapping("/admin/comments/{commentId}")
    ApiResponse<Void> adminDeleteComment(@PathVariable String commentId) {
        commentService.adminDeleteComment(commentId);
        return ApiResponse.<Void>builder().build();
    }
}