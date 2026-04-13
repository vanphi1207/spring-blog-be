package me.ihqqq.spring_blog.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.spring_blog.dto.request.CategoryRequest;
import me.ihqqq.spring_blog.dto.response.ApiResponse;
import me.ihqqq.spring_blog.dto.response.CategoryResponse;
import me.ihqqq.spring_blog.dto.response.PostSummaryResponse;
import me.ihqqq.spring_blog.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {

    CategoryService categoryService;

    /**
     * GET /categories
     * Lấy tất cả categories (public).
     */
    @GetMapping
    ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAllCategories())
                .build();
    }

    /**
     * GET /categories/{slug}
     * Lấy chi tiết một category theo slug (public).
     */
    @GetMapping("/{slug}")
    ApiResponse<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getCategoryBySlug(slug))
                .build();
    }

    /**
     * GET /categories/{slug}/posts
     * Lấy danh sách published posts thuộc category (public).
     */
    @GetMapping("/{slug}/posts")
    ApiResponse<Page<PostSummaryResponse>> getPostsByCategory(
            @PathVariable String slug,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.<Page<PostSummaryResponse>>builder()
                .result(categoryService.getPostsByCategory(slug, pageable))
                .build();
    }

    /**
     * POST /categories
     * Tạo category mới — chỉ ADMIN.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<CategoryResponse> createCategory(@RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.createCategory(request))
                .build();
    }

    /**
     * PUT /categories/{id}
     * Cập nhật category — chỉ ADMIN.
     */
    @PutMapping("/{id}")
    ApiResponse<CategoryResponse> updateCategory(
            @PathVariable String id,
            @RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.updateCategory(id, request))
                .build();
    }

    /**
     * DELETE /categories/{id}
     * Xóa category — chỉ ADMIN.
     * Posts thuộc category sẽ có category = null.
     */
    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ApiResponse.<Void>builder()
                .message("Category deleted successfully")
                .build();
    }
}