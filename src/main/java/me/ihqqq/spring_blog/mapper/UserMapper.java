package me.ihqqq.spring_blog.mapper;

import me.ihqqq.spring_blog.dto.request.UserCreationRequest;
import me.ihqqq.spring_blog.dto.request.UserUpdateRequest;
import me.ihqqq.spring_blog.dto.response.UserResponse;
import me.ihqqq.spring_blog.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

}
