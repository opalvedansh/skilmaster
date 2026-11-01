package com.skillmate.dto;

import com.skillmate.entity.Role;
import com.skillmate.entity.User;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public final class AuthDtos {
    private AuthDtos() {}
    public static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$";
    public static final String PASSWORD_MESSAGE = "Password must be 8-72 characters and contain at least one letter and one number";

    public record RegisterRequest(
            @NotBlank(message = "Name is required") @Size(max = 100, message = "Name must be at most 100 characters") String name,
            @NotBlank(message = "Email is required") @Email(message = "Email format is invalid") @Size(max = 150) String email,
            @NotBlank(message = "Password is required") @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE) String password) {}

    public record LoginRequest(
            @NotBlank(message = "Email is required") String email,
            @NotBlank(message = "Password is required") String password) {}

    public record UserDto(Long id, String name, String email, Role role, LocalDateTime createdAt) {
        public static UserDto from(User u) { return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getCreatedAt()); }
    }

    public record CreateUserRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE) String password,
            @NotNull Role role) {}

    public record UpdateUserRequest(
            @Size(max = 100) String name,
            @Email @Size(max = 150) String email,
            Role role,
            @Pattern(regexp = PASSWORD_PATTERN, message = PASSWORD_MESSAGE) String password,
            String currentPassword) {}

    public record PageResponse<T>(java.util.List<T> items, int page, int size, long totalElements, int totalPages) {
        public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> p) {
            return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
        }
    }
}
