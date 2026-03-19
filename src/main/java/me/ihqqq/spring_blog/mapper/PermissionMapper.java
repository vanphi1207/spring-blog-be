package me.ihqqq.spring_blog.mapper;

import me.ihqqq.spring_blog.dto.request.PermissionRequest;
import me.ihqqq.spring_blog.dto.response.PermissionResponse;
import me.ihqqq.spring_blog.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);
}
