package me.ihqqq.spring_blog.repository;

import me.ihqqq.spring_blog.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, String> {

    Optional<PostLike> findByPostIdAndUserId(String postId, String userId);

    boolean existsByPostIdAndUserId(String postId, String userId);

    long countByPostId(String postId);

    void deleteByPostIdAndUserId(String postId, String userId);

    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId")
    long countLikesByPostId(String postId);

    @Query("SELECT pl.post.id, COUNT(pl) FROM PostLike pl WHERE pl.post.id IN :postIds GROUP BY pl.post.id")
    List<Object[]> countLikesByPostIds(@Param("postIds") List<String> postIds);

    default Map<String, Long> getLikeCountMap(List<String> postIds) {
        return countLikesByPostIds(postIds).stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }
}