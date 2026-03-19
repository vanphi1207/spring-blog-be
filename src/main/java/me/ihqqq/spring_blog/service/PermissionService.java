package me.ihqqq.spring_blog.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.spring_blog.dto.request.PermissionRequest;
import me.ihqqq.spring_blog.dto.response.PermissionResponse;
import me.ihqqq.spring_blog.entity.Permission;
import me.ihqqq.spring_blog.mapper.PermissionMapper;
import me.ihqqq.spring_blog.repository.PermissionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionMapper permissionMapper;
    PermissionRepository permissionRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public PermissionResponse create(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<PermissionResponse> getAll() {
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String permission) {
        permissionRepository.deleteById(permission);
    }
}
