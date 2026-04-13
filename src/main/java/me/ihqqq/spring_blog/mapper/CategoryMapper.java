package me.ihqqq.spring_blog.mapper;

import me.ihqqq.spring_blog.dto.request.CategoryRequest;
import me.ihqqq.spring_blog.dto.response.CategoryResponse;
import me.ihqqq.spring_blog.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "posts", ignore = true)
    Category toCategory(CategoryRequest request);

    @Mapping(target = "postCount", ignore = true)
    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "posts", ignore = true)
    void updateCategory(@MappingTarget Category category, CategoryRequest request);
}
