package me.ihqqq.spring_blog.repository;

import me.ihqqq.spring_blog.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {

    Optional<Tag> findBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    // Tìm tag theo name (case-insensitive) để tránh duplicate khi tạo
    Optional<Tag> findByNameIgnoreCase(String name);

    // Lấy tất cả tags có ít nhất 1 published post (dùng cho tag cloud)
    @Query("SELECT t FROM Tag t WHERE SIZE(t.posts) > 0")
    List<Tag> findAllWithPosts();

    // Đếm published posts của tag
    @Query("SELECT COUNT(p) FROM Post p JOIN p.tags t WHERE t.id = :tagId AND p.status = 'PUBLISHED'")
    long countPublishedPostsByTagId(@Param("tagId") String tagId);
}