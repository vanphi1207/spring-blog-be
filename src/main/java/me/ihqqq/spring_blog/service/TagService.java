package me.ihqqq.spring_blog.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.spring_blog.dto.request.TagRequest;
import me.ihqqq.spring_blog.dto.response.PostSummaryResponse;
import me.ihqqq.spring_blog.dto.response.TagResponse;
import me.ihqqq.spring_blog.entity.Tag;
import me.ihqqq.spring_blog.exception.AppException;
import me.ihqqq.spring_blog.exception.ErrorCode;
import me.ihqqq.spring_blog.mapper.PostMapper;
import me.ihqqq.spring_blog.mapper.TagMapper;
import me.ihqqq.spring_blog.repository.PostRepository;
import me.ihqqq.spring_blog.repository.TagRepository;
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
public class TagService {

    TagRepository tagRepository;
    PostRepository postRepository;
    TagMapper tagMapper;
    PostMapper postMapper;

    /**
     * Lấy tất cả tags kèm số lượng published posts. (public endpoint)
     */
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::toTagResponseWithCount)
                .toList();
    }

    /**
     * Lấy tag theo slug (public endpoint)
     */
    @Transactional(readOnly = true)
    public TagResponse getTagBySlug(String slug) {
        Tag tag = tagRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.TAG_NOT_FOUND));
        return toTagResponseWithCount(tag);
    }

    /**
     * Lấy danh sách published posts của một tag (theo slug). (public endpoint)
     */
    public Page<PostSummaryResponse> getPostsByTag(String tagSlug, Pageable pageable) {
        if(!tagRepository.findBySlug(tagSlug).isPresent()) {
            throw new AppException(ErrorCode.TAG_NOT_FOUND);
        }
        return postRepository.findPublishedByTagSlug(tagSlug, pageable)
                .map(postMapper::toPostSummaryResponse);
    }

    /**
     * Tạo tag mới - chỉ ADMIN
     * Tự động generate slug từ name
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TagResponse createTag(TagRequest request) {
        //Case-insensitive check
        if(tagRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.TAG_NAME_EXISTED);
        }

        String slug = generateUniqueSlug(request.getName());

        Tag tag = tagMapper.toTag(request);
        tag.setSlug(slug);

        Tag saved = tagRepository.save(tag);
        log.info("Tag created: {} (slug: {})", saved.getName(), saved.getSlug());

        return toTagResponseWithCount(saved);

    }

    /**
     * Cập nhật tag - chỉ ADMIN
     * Nếu name thay đổi thì regenerate slug.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TagResponse updateTag(String id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TAG_NOT_FOUND));

        //Kiểm tra name trùng với tag khác (case-insensitive)
        if(!tag.getName().equalsIgnoreCase(request.getName())) {
            tagRepository.findByNameIgnoreCase(request.getName()).ifPresent(existingTag -> {
                if(existingTag.getId().equals(id)) {
                    throw new AppException(ErrorCode.TAG_NAME_EXISTED);
                }
            });

            tag.setSlug(generateUniqueSlug(request.getName()));
        }

        tagMapper.updateTag(tag, request);
        Tag saved = tagRepository.save(tag);

        log.info("Tag updated: {} (id: {})", saved.getName(), id);
        return toTagResponseWithCount(saved);
    }

    /**
     * Xóa tag - chỉ ADMIN
     * Tự động xóa các bản ghi trong bảng post_tags
     * Không xóa các post liên quan
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteTag(String id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.TAG_NOT_FOUND));

        //Detach tag khỏi tất cả posts trước khi xóa để tránh contraint violation
        tag.getPosts().forEach(post -> post.getTags().remove(tag));

        tagRepository.delete(tag);
        log.warn("Tag deleted: {} (id: {})", tag.getName(), id);
    }



    private TagResponse toTagResponseWithCount(Tag tag) {
        TagResponse response = tagMapper.toTagResponse(tag);
        response.setPostCount(tagRepository.countPublishedPostsByTagId(tag.getId()));
        return response;
    }

    private String generateUniqueSlug(String name) {
        String baseSlug = SlugUtils.toSlug(name);
        String slug = baseSlug;
        int counter = 1;

        while(tagRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }
}
