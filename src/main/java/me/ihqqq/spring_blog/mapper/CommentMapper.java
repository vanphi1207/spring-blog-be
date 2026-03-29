package me.ihqqq.spring_blog.mapper;

import me.ihqqq.spring_blog.dto.response.CommentResponse;
import me.ihqqq.spring_blog.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "replies", ignore = true)
    CommentResponse toCommentResponse(Comment comment);
}
