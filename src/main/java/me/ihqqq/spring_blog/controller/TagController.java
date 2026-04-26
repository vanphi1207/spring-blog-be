package me.ihqqq.spring_blog.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.spring_blog.dto.request.TagRequest;
import me.ihqqq.spring_blog.dto.response.ApiResponse;
import me.ihqqq.spring_blog.dto.response.PageResponse;
import me.ihqqq.spring_blog.dto.response.PostSummaryResponse;
import me.ihqqq.spring_blog.dto.response.TagResponse;
import me.ihqqq.spring_blog.service.TagService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TagController {

    TagService tagService;

    /**
     * GET /tags
     * Lấy tất cả
     */
    @GetMapping
    ApiResponse<List<TagResponse>> getAllTags() {
        return ApiResponse.<List<TagResponse>>builder()
                .result(tagService.getAllTags())
                .build();
    }

    /**
     * GET /tags/{slug}
     * Lấy chi tiết một tag theo slug (public).
     */

    @GetMapping("/{slug}")
    ApiResponse<TagResponse> getTagBySlug(@PathVariable String slug) {
        return ApiResponse.<TagResponse>builder()
                .result(tagService.getTagBySlug(slug))
                .build();
    }

    /**
     * GET /tags/{slug}/posts
     * Lấy danh sách published posts có tag này (public)
     */
    @GetMapping("/{slug}/posts")
    ApiResponse<PageResponse<PostSummaryResponse>> getPostsByTag(
            @PathVariable String slug,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<PageResponse<PostSummaryResponse>>builder()
                .result(PageResponse.of(tagService.getPostsByTag(slug, pageable)))
                .build();
    }

    /**
     * POST /tags
     * tạo tag mới - chỉ ADMIN
     */
    @PostMapping
    ApiResponse<TagResponse> createTag(@RequestBody @Valid TagRequest request) {
        return ApiResponse.<TagResponse>builder()
                .result(tagService.createTag(request))
                .build();
    }

    /**
     * PUT /tags/{id}
     * Cập nhật tag - chỉ ADMIN
     */
    @PutMapping("/{id}")
    ApiResponse<TagResponse> updateTag(
            @PathVariable String id,
            @RequestBody @Valid TagRequest request) {
        return ApiResponse.<TagResponse>builder()
                .result(tagService.updateTag(id, request))
                .build();
    }

    /**
     * DELETE /tags/{id}
     * Xóa tag - chỉ ADMIN
     * Tự động gỡ tag khỏi tất cả posts, không xóa posts
     */
    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteTag(@PathVariable String id) {
        tagService.deleteTag(id);
        return ApiResponse.<Void>builder()
                .message("Tag deleted successfully")
                .build();
    }




}
