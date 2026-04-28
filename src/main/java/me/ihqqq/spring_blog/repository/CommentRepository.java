package me.ihqqq.spring_blog.repository;

import me.ihqqq.spring_blog.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository  extends JpaRepository<Comment, String> {

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL AND c.deleted = false ORDER BY c.createdAt ASC")
    Page<Comment> findTopLevelByPostId(@Param("postId") String postId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.parent.id = :parentId AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentId(@Param("parentId") String parentId);

    @Query("""
            SELECT c FROM Comment c
            WHERE c.parent.id IN :parentIds
              AND c.deleted = false
            ORDER BY c.createdAt ASC
            """)
    List<Comment> findRepliesByParentIdIn(@Param("parentIds") List<String> parentIds);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.deleted = false")
    long countByPostId(@Param("postId") String postId);

    Page<Comment> findByPostId(String postId, Pageable pageable);

}
