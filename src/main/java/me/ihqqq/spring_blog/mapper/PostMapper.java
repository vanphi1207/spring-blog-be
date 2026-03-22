package me.ihqqq.spring_blog.mapper;

import me.ihqqq.spring_blog.dto.request.PostRequest;
import me.ihqqq.spring_blog.dto.response.PostResponse;
import me.ihqqq.spring_blog.dto.response.PostSummaryResponse;
import me.ihqqq.spring_blog.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "status", ignore = true)
    Post toPost(PostRequest request);

    PostResponse toPostResponse(Post post);

    PostSummaryResponse toPostSummaryResponse(Post post);

    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updatePost(@MappingTarget Post post, PostRequest request);

}
