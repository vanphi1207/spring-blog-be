package me.ihqqq.spring_blog.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.spring_blog.constant.PostStatus;
import me.ihqqq.spring_blog.dto.request.PostRequest;
import me.ihqqq.spring_blog.dto.response.ApiResponse;
import me.ihqqq.spring_blog.dto.response.PostResponse;
import me.ihqqq.spring_blog.dto.response.PostSummaryResponse;
import me.ihqqq.spring_blog.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/posts")
public class PostController {

    PostService postService;

    @GetMapping
    ApiResponse<Page<PostSummaryResponse>> getPublishedPosts(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ApiResponse.<Page<PostSummaryResponse>>builder()
                .result(postService.getPublishedPosts(pageable))
                .build();
    }

    @GetMapping("/{slug}")
    ApiResponse<PostResponse> getPostBySlug(@PathVariable String slug) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.getPostBySlug(slug))
                .build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<PostResponse> createPost(@RequestBody @Valid PostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.createPost(request))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<PostResponse> updatePost(@PathVariable String id, @RequestBody @Valid PostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.updatePost(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deletePost(@PathVariable String id) {
        postService.deletedPost(id);
        return ApiResponse.<Void>builder().build();
    }

    @PatchMapping("/{id}/publish")
    ApiResponse<PostResponse> publishPost(@PathVariable String id) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.publishPost(id))
                .build();
    }

    @PatchMapping("/{id}/unpublish")
    ApiResponse<PostResponse> unpublishPost(@PathVariable String id) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.unpublishPost(id))
                .build();
    }

    @GetMapping("/me/posts")
    ApiResponse<Page<PostSummaryResponse>> getMyPosts(
            @RequestParam(required = false) PostStatus status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<Page<PostSummaryResponse>>builder()
                .result(postService.getMyPosts(status, pageable))
                .build();

    }

    @GetMapping("/admin/posts")
    ApiResponse<Page<PostSummaryResponse>> getAllPosts(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<Page<PostSummaryResponse>>builder()
                .result(postService.getAllPosts(pageable))
                .build();

    }

    @GetMapping("/admin/posts/{id}")
    ApiResponse<PostResponse> adminGetPost(@PathVariable String id) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.adminGetPost(id))
                .build();
    }

    @DeleteMapping("/admin/posts/{id}")
    ApiResponse<Void> adminDeletePost(@PathVariable String id) {
        postService.adminDeletedPost(id);
        return ApiResponse.<Void>builder().build();
    }
}
