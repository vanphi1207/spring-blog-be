package me.ihqqq.spring_blog.repository;

import me.ihqqq.spring_blog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {

    Optional<Category> findBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.category.id = :categoryId AND p.status = 'PUBLISHED'")
    long countPublishedPostsByCategoryId(@Param("categoryId") String categoryId);
}
