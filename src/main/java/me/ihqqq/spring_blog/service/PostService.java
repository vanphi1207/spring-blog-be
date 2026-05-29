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
import me.ihqqq.spring_blog.repository.PostLikeRepository;
import me.ihqqq.spring_blog.repository.PostRepository;
import me.ihqqq.spring_blog.repository.TagRepository;
import me.ihqqq.spring_blog.repository.UserRepository;
import me.ihqqq.spring_blog.util.ReadingTimeUtils;
import me.ihqqq.spring_blog.util.SlugUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    PostLikeRepository postLikeRepository;
    PostMapper postMapper;


    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getPublishedPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findByStatus(PostStatus.PUBLISHED, pageable);
        return enrichPageSummary(posts);
    }

    @Transactional
    public PostResponse getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        return enrichResponse(postMapper.toPostResponse(post), post.getId());
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> searchPosts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return getPublishedPosts(pageable);
        }
        Page<Post> posts = postRepository.searchPublished(keyword.trim(), pageable);
        return enrichPageSummary(posts);
    }


    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getRelatedPosts(String postId, int limit) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        String categoryId = post.getCategory() != null ? post.getCategory().getId() : null;
        List<String> tagIds = post.getTags().stream().map(Tag::getId).toList();

        List<Post> related;

        if (categoryId != null || !tagIds.isEmpty()) {
            related = postRepository.findRelatedPosts(
                    postId,
                    categoryId != null ? categoryId : "NONE",
                    tagIds.isEmpty() ? List.of("NONE") : tagIds,
                    PageRequest.of(0, limit)
            );
        } else {
            related = Collections.emptyList();
        }

        if (related.size() < limit) {
            List<Post> latest = postRepository.findLatestExcluding(postId, PageRequest.of(0, limit));
            Set<String> existingIds = new HashSet<>();
            related.forEach(p -> existingIds.add(p.getId()));

            List<Post> mutableRelated = new java.util.ArrayList<>(related);
            for (Post p : latest) {
                if (mutableRelated.size() >= limit) break;
                if (!existingIds.contains(p.getId())) {
                    mutableRelated.add(p);
                    existingIds.add(p.getId());
                }
            }
            related = mutableRelated;
        }

        List<String> relatedIds = related.stream().map(Post::getId).toList();
        Map<String, Long> likeCountMap = postLikeRepository.getLikeCountMap(relatedIds);

        return related.stream()
                .map(p -> {
                    PostSummaryResponse summary = postMapper.toPostSummaryResponse(p);
                    summary.setLikeCount(likeCountMap.getOrDefault(p.getId(), 0L));
                    return summary;
                })
                .toList();
    }


    @Transactional
    public PostResponse createPost(PostRequest request) {
        User author = getCurrentUser();
        String slug = generateUniqueSlug(request.getTitle());

        Post post = postMapper.toPost(request);
        post.setSlug(slug);
        post.setAuthor(author);
        post.setStatus(request.getStatus() != null ? request.getStatus() : PostStatus.DRAFT);
        post.setReadingTime(ReadingTimeUtils.calculate(request.getContent()));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            post.setCategory(category);
        }

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            if (request.getTagIds().size() > 10) {
                throw new AppException(ErrorCode.TAG_LIMIT_EXCEEDED);
            }
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            post.setTags(tags);
        }

        Post saved = postRepository.save(post);
        return enrichResponse(postMapper.toPostResponse(saved), saved.getId());
    }

    @Transactional
    public PostResponse updatePost(String id, PostRequest request) {
        Post post = getPostAndCheckOwnership(id);

        if (!post.getTitle().equals(request.getTitle())) {
            post.setSlug(generateUniqueSlug(request.getTitle()));
        }

        postMapper.updatePost(post, request);
        post.setReadingTime(ReadingTimeUtils.calculate(request.getContent()));

        if (request.getStatus() != null) {
            post.setStatus(request.getStatus());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            post.setCategory(category);
        } else {
            post.setCategory(null);
        }

        if (request.getTagIds() != null) {
            if (request.getTagIds().size() > 10) {
                throw new AppException(ErrorCode.TAG_LIMIT_EXCEEDED);
            }
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            post.setTags(tags);
        } else {
            post.getTags().clear();
        }

        Post saved = postRepository.save(post);
        return enrichResponse(postMapper.toPostResponse(saved), saved.getId());
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
        Post saved = postRepository.save(post);
        return enrichResponse(postMapper.toPostResponse(saved), saved.getId());
    }

    @Transactional
    public PostResponse unpublishPost(String id) {
        Post post = getPostAndCheckOwnership(id);
        post.setStatus(PostStatus.DRAFT);
        Post saved = postRepository.save(post);
        return enrichResponse(postMapper.toPostResponse(saved), saved.getId());
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getMyPosts(PostStatus status, Pageable pageable) {
        User author = getCurrentUser();

        Page<Post> posts = (status != null)
                ? postRepository.findByAuthorIdAndStatus(author.getId(), status, pageable)
                : postRepository.findByAuthorId(author.getId(), pageable);

        return enrichPageSummary(posts);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getAllPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return enrichPageSummary(posts);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void adminDeletedPost(String id) {
        if (!postRepository.existsById(id)) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }
        postRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public PostResponse adminGetPost(String id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        return enrichResponse(postMapper.toPostResponse(post), post.getId());
    }


    private Page<PostSummaryResponse> enrichPageSummary(Page<Post> posts) {
        List<String> postIds = posts.map(Post::getId).toList();

        Map<String, Long> likeCountMap = postLikeRepository.getLikeCountMap(postIds);

        return posts.map(post -> {
            PostSummaryResponse summary = postMapper.toPostSummaryResponse(post);
            summary.setLikeCount(likeCountMap.getOrDefault(post.getId(), 0L));
            return summary;
        });
    }


    private PostResponse enrichResponse(PostResponse response, String postId) {
        response.setLikeCount(postLikeRepository.countByPostId(postId));
        return response;
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

        if (!isAdmin && !post.getAuthor().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return post;
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