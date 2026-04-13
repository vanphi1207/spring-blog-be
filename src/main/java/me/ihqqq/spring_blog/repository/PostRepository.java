package me.ihqqq.spring_blog.repository;

import me.ihqqq.spring_blog.constant.PostStatus;
import me.ihqqq.spring_blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {

    Optional<Post> findBySlug(String slug);

    boolean existsBySlug(String slug);

    // List published posts (public)
    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    // List posts của một author
    Page<Post> findByAuthorId(String authorId, Pageable pageable);

    // Author xem posts của chính mình theo status
    Page<Post> findByAuthorIdAndStatus(String authorId, PostStatus status, Pageable pageable);

    // Filter theo category (chỉ published)
    Page<Post> findByStatusAndCategoryId(PostStatus status, String categoryId, Pageable pageable);

    // Filter theo category slug (chỉ published)
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' AND p.category.slug = :categorySlug")
    Page<Post> findPublishedByCategorySlug(@Param("categorySlug") String categorySlug, Pageable pageable);

    // Filter theo tag slug (chỉ published)
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE p.status = 'PUBLISHED' AND t.slug = :tagSlug")
    Page<Post> findPublishedByTagSlug(@Param("tagSlug") String tagSlug, Pageable pageable);

    // Search theo title hoặc excerpt (chỉ published)
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.excerpt) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Post> searchPublished(@Param("keyword") String keyword, Pageable pageable);
}