package me.ihqqq.spring_blog.mapper;

import me.ihqqq.spring_blog.dto.request.TagRequest;
import me.ihqqq.spring_blog.dto.response.TagResponse;
import me.ihqqq.spring_blog.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TagMapper {

    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "posts", ignore = true)
    Tag toTag(TagRequest request);

    @Mapping(target = "postCount", ignore = true)
    TagResponse toTagResponse(Tag tag);

    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "posts", ignore = true)
    void updateTag(@MappingTarget Tag tag, TagRequest request);
}