package me.ihqqq.spring_blog.repository;

import me.ihqqq.spring_blog.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedRepository extends JpaRepository<InvalidatedToken, String> {

}
