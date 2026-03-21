package me.ihqqq.spring_blog.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.ihqqq.spring_blog.dto.request.UserCreationRequest;
import me.ihqqq.spring_blog.dto.request.UserProfileUpdateRequest;
import me.ihqqq.spring_blog.dto.request.UserUpdateRequest;
import me.ihqqq.spring_blog.dto.response.ApiResponse;
import me.ihqqq.spring_blog.dto.response.AvatarSignatureResponse;
import me.ihqqq.spring_blog.dto.response.UserResponse;
import me.ihqqq.spring_blog.service.CloudinaryService;
import me.ihqqq.spring_blog.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;
    CloudinaryService cloudinaryService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.setResult(userService.createUser(request));
        return response;
    }

    @PutMapping("/{userId}")
    ApiResponse<UserResponse> updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @PatchMapping("/my-profile")
    ApiResponse<UserResponse> updateMyProfile(@RequestBody @Valid UserProfileUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateMyProfile(request))
                .build();
    }

    // Bước 1: Client xin signature để upload thẳng lên Cloudinary
    @GetMapping("/my-profile/avatar/signature")
    ApiResponse<AvatarSignatureResponse> getAvatarUploadSignature() {
        return ApiResponse.<AvatarSignatureResponse>builder()
                .result(cloudinaryService.generateUploadSignature())
                .build();
    }

    // Bước 2: Sau khi client upload xong, gửi URL lên để lưu vào DB
    @PatchMapping("/my-profile/avatar")
    ApiResponse<UserResponse> updateAvatarUrl(@RequestParam String avatarUrl) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateAvatarUrl(avatarUrl))
                .build();
    }


    @GetMapping
    ApiResponse<List<UserResponse>> getAllUsers() {
        ApiResponse<List<UserResponse>> response = new ApiResponse<>();
        response.setResult(userService.getAllUsers());
        return response;
    }

    @GetMapping("/{userId}")
    ApiResponse<UserResponse> getUserById(@PathVariable String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserById(userId))
                .build();
    }

    @DeleteMapping("/{userId}")
    ApiResponse<Void> deleteUserById(@PathVariable String userId) {
        userService.deleteUserById(userId);
        return ApiResponse.<Void>builder()
                .message("User has been deleted")
                .build();
    }

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

}
