package me.ihqqq.spring_blog.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.spring_blog.dto.request.CategoryRequest;
import me.ihqqq.spring_blog.dto.response.CategoryResponse;
import me.ihqqq.spring_blog.dto.response.PostSummaryResponse;
import me.ihqqq.spring_blog.entity.Category;
import me.ihqqq.spring_blog.exception.AppException;
import me.ihqqq.spring_blog.exception.ErrorCode;
import me.ihqqq.spring_blog.mapper.CategoryMapper;
import me.ihqqq.spring_blog.mapper.PostMapper;
import me.ihqqq.spring_blog.repository.CategoryRepository;
import me.ihqqq.spring_blog.repository.PostRepository;
import me.ihqqq.spring_blog.util.SlugUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryService {

    CategoryRepository categoryRepository;
    PostRepository postRepository;
    CategoryMapper categoryMapper;
    PostMapper postMapper;

    /**
     * Lấy tất cả categories kèm số lượng published posts. (public endpoint)
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toCategoryResponseWithCount)
                .toList();
    }

    /**
     * Lấy một category theo slug. (public endpoint)
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return toCategoryResponseWithCount(category);
    }

    /**
     * Lấy danh sách published posts của một category (theo slug). (public endpoint)
     */
    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getPostsByCategory(String categorySlug, Pageable pageable) {
        // Validate category tồn tại
        if (!categoryRepository.findBySlug(categorySlug).isPresent()) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return postRepository.findPublishedByCategorySlug(categorySlug, pageable)
                .map(postMapper::toPostSummaryResponse);
    }

    /**
     * Tạo category mới — chỉ ADMIN.
     * Tự động generate slug từ name.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTED);
        }

        String slug = generateUniqueSlug(request.getName());

        Category category = categoryMapper.toCategory(request);
        category.setSlug(slug);

        Category saved = categoryRepository.save(category);
        log.info("Category created: {} (slug: {})", saved.getName(), saved.getSlug());

        return toCategoryResponseWithCount(saved);
    }

    /**
     * Cập nhật category — chỉ ADMIN.
     * Nếu name thay đổi thì regenerate slug.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Kiểm tra name trùng với category khác
        if (!category.getName().equals(request.getName())
                && categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTED);
        }

        // Regenerate slug nếu name thay đổi
        if (!category.getName().equals(request.getName())) {
            category.setSlug(generateUniqueSlug(request.getName()));
        }

        categoryMapper.updateCategory(category, request);
        Category saved = categoryRepository.save(category);

        log.info("Category updated: {} (id: {})", saved.getName(), id);
        return toCategoryResponseWithCount(saved);
    }

    /**
     * Xóa category — chỉ ADMIN.
     * Các posts thuộc category này sẽ có category = null (set null, không xóa post).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Detach category khỏi tất cả posts trước khi xóa
        category.getPosts().forEach(post -> post.setCategory(null));

        categoryRepository.delete(category);
        log.warn("Category deleted: {} (id: {})", category.getName(), id);
    }


    private CategoryResponse toCategoryResponseWithCount(Category category) {
        CategoryResponse response = categoryMapper.toCategoryResponse(category);
        response.setPostCount(categoryRepository.countPublishedPostsByCategoryId(category.getId()));
        return response;
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = SlugUtils.toSlug(name);
        String slug = baseSlug;
        int counter = 1;

        while (categoryRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }
}