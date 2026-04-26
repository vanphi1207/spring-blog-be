package me.ihqqq.spring_blog.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.ihqqq.spring_blog.constant.PostStatus;
import me.ihqqq.spring_blog.dto.request.PostRequest;
import me.ihqqq.spring_blog.dto.response.PostResponse;
import me.ihqqq.spring_blog.dto.response.PostSummaryResponse;
import me.ihqqq.spring_blog.entity.Category;
import me.ihqqq.spring_blog.entity.Post;
import me.ihqqq.spring_blog.entity.Tag;
import me.ihqqq.spring_blog.entity.User;
import me.ihqqq.spring_blog.exception.AppException;
import me.ihqqq.spring_blog.exception.ErrorCode;
import me.ihqqq.spring_blog.mapper.PostMapper;
import me.ihqqq.spring_blog.repository.CategoryRepository;
import me.ihqqq.spring_blog.repository.PostRepository;
import me.ihqqq.spring_blog.repository.TagRepository;
import me.ihqqq.spring_blog.repository.UserRepository;
import me.ihqqq.spring_blog.util.SlugUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostService {

    PostRepository postRepository;
    UserRepository userRepository;
    CategoryRepository categoryRepository;
    TagRepository tagRepository;
    PostMapper postMapper;

    public Page<PostSummaryResponse> getPublishedPosts(Pageable pageable) {
        return postRepository.findByStatus(PostStatus.PUBLISHED, pageable).map(postMapper::toPostSummaryResponse);
    }

    @Transactional
    public PostResponse getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        if(post.getStatus() != PostStatus.PUBLISHED) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        return postMapper.toPostResponse(post);
    }

    @Transactional
    public PostResponse createPost(PostRequest request) {
        User author = getCurrentUser();
        String slug = generateUniqueSlug(request.getTitle());

        Post post = postMapper.toPost(request);
        post.setSlug(slug);
        post.setAuthor(author);
        post.setStatus(request.getStatus() != null ? request.getStatus() : PostStatus.DRAFT);

        if(request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            post.setCategory(category);
        }

        if(request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            if(request.getTagIds().size() > 10) {
                throw new AppException(ErrorCode.TAG_LIMIT_EXCEEDED);
            }
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            post.setTags(tags);
        }

        return postMapper.toPostResponse(postRepository.save(post));
    }

    @Transactional
    public PostResponse updatePost(String id, PostRequest request) {
        Post post = getPostAndCheckOwnership(id);

        //Nếu title thay đổi thì regenerate slug
        if(!post.getTitle().equals(request.getTitle())) {
            post.setSlug(generateUniqueSlug(request.getTitle()));
        }

        postMapper.updatePost(post, request);

        if(request.getStatus() != null) {
            post.setStatus(request.getStatus());
        }

        if(request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            post.setCategory(category);
        } else {
            post.setCategory(null);
        }

        if(request.getTagIds() != null) {
            if(request.getTagIds().size() > 10) {
                throw new AppException(ErrorCode.TAG_LIMIT_EXCEEDED);
            }
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            post.setTags(tags);
        } else {
            post.getTags().clear();
        }

        return postMapper.toPostResponse(postRepository.save(post));

    }

    @Transactional
    public void deletedPost(String id) {
        Post post = getPostAndCheckOwnership(id);
        postRepository.delete(post);
    }

    @Transactional
    public PostResponse publishPost(String id) {
        Post post = getPostAndCheckOwnership(id);
        post.setStatus(PostStatus.PUBLISHED);
        return postMapper.toPostResponse(postRepository.save(post));
    }

    @Transactional
    public PostResponse unpublishPost(String id) {
        Post post = getPostAndCheckOwnership(id);
        post.setStatus(PostStatus.DRAFT);
        return postMapper.toPostResponse(postRepository.save(post));
    }

    public Page<PostSummaryResponse> getMyPosts(PostStatus status, Pageable pageable) {
        User author = getCurrentUser();

        Page<Post> posts = (status != null)
                ? postRepository.findByAuthorIdAndStatus(author.getId(), status, pageable)
                : postRepository.findByAuthorId(author.getId(), pageable);

        return posts.map(postMapper::toPostSummaryResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<PostSummaryResponse> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(postMapper::toPostSummaryResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void adminDeletedPost(String id) {
        if(!postRepository.existsById(id)) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }
        postRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PostResponse adminGetPost(String id) {
        return postMapper.toPostResponse(postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND)));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private Post getPostAndCheckOwnership(String id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        log.info("isAdmin {}", isAdmin);
        log.info("currentUsername {}", currentUsername);
        log.info("authorUsername {}", post.getAuthor().getUsername());

        if(!isAdmin && !post.getAuthor().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);

        }

        return post;
    }
    
    public Page<PostSummaryResponse> searchPosts(String keyword, Pageable pageable) {
        if(keyword == null || keyword.isBlank()) {
            return getPublishedPosts(pageable);
        }
        return postRepository.searchPublished(keyword.trim(), pageable)
                .map(postMapper::toPostSummaryResponse);
    }


    private String generateUniqueSlug(String title) {
        String baseSlug = SlugUtils.toSlug(title);
        String slug = baseSlug;
        int counter = 1;

        while (postRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }


}
