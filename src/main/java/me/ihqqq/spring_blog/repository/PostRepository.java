package me.ihqqq.spring_blog.repository;

import me.ihqqq.spring_blog.constant.PostStatus;
import me.ihqqq.spring_blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post,String> {

    Optional<Post> findBySlug(String slug);

    boolean existsBySlug(String slug);

    //List post của xuất bản (public)
    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    //List posts của một author
    Page<Post> findByAuthorId(String authorId, Pageable pageable);

    //Author xem posts của chính mình theo status
    Page<Post> findByAuthorIdAndStatus(String authorId, PostStatus status, Pageable pageable);


}
