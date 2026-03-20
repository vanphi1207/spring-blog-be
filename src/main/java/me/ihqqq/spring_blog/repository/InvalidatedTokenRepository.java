package me.ihqqq.spring_blog.repository;

import me.ihqqq.spring_blog.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {

}
