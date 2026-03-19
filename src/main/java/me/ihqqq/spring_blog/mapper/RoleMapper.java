package me.ihqqq.spring_blog.mapper;

import me.ihqqq.spring_blog.dto.request.RoleRequest;
import me.ihqqq.spring_blog.dto.response.RoleResponse;
import me.ihqqq.spring_blog.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
