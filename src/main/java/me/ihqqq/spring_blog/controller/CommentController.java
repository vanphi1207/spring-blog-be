package me.ihqqq.spring_blog.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.spring_blog.dto.request.CommentRequest;
import me.ihqqq.spring_blog.dto.request.CommentUpdateRequest;
import me.ihqqq.spring_blog.dto.response.ApiResponse;
import me.ihqqq.spring_blog.dto.response.CommentResponse;
import me.ihqqq.spring_blog.dto.response.PageResponse;
import me.ihqqq.spring_blog.service.CommentService;
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
    
    @GetMapping("/posts/{postId}/comments")
    ApiResponse<PageResponse<CommentResponse>> getComments(
            @PathVariable String postId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.<PageResponse<CommentResponse>>builder()
                .result(PageResponse.of(commentService.getCommentsByPost(postId, pageable)))
                .build();
    }

    @GetMapping("/posts/{postId}/comments/count")
    ApiResponse<Long> countComments(@PathVariable String postId) {
        return ApiResponse.<Long>builder()
                .result(commentService.countCommentsByPost(postId))
                .build();
    }


    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<CommentResponse> createComment(
            @PathVariable String postId,
            @RequestBody @Valid CommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.createComment(postId, request))
                .build();
    }

    @PutMapping("/comments/{commentId}")
    ApiResponse<CommentResponse> updateComment(
            @PathVariable String commentId,
            @RequestBody @Valid CommentUpdateRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.updateComment(commentId, request))
                .build();
    }
    @DeleteMapping("/comments/{commentId}")
    ApiResponse<Void> deleteComment(@PathVariable String commentId) {
        commentService.deleteComment(commentId);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/admin/posts/{postId}/comments")
    ApiResponse<PageResponse<CommentResponse>> adminGetComments(
            @PathVariable String postId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<PageResponse<CommentResponse>>builder()
                .result(PageResponse.of(commentService.adminGetCommentsByPost(postId, pageable)))
                .build();
    }

    @DeleteMapping("/admin/comments/{commentId}")
    ApiResponse<Void> adminDeleteComment(@PathVariable String commentId) {
        commentService.adminDeleteComment(commentId);
        return ApiResponse.<Void>builder().build();
    }
}