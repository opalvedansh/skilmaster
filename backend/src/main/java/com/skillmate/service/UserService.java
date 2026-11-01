package com.skillmate.service;

import com.skillmate.dto.AuthDtos.*;
import com.skillmate.entity.Role;
import com.skillmate.entity.User;
import com.skillmate.exception.ApiException;
import com.skillmate.repository.UserRepository;
import com.skillmate.security.UserPrincipal;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository users; private final PasswordEncoder encoder;
    public UserService(UserRepository users, PasswordEncoder encoder) { this.users = users; this.encoder = encoder; }

    public PageResponse<UserDto> list(String q, Role role, int page, int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("id"));
        return PageResponse.of(users.search(q == null ? "" : q.trim(), role, pageable).map(UserDto::from));
    }

    public UserDto get(Long id) { return UserDto.from(find(id)); }

    @Transactional
    public UserDto create(CreateUserRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) throw ApiException.conflict("An account with this email already exists");
        User u = new User();
        u.setName(r.name().trim()); u.setEmail(email); u.setPassword(encoder.encode(r.password())); u.setRole(r.role());
        return UserDto.from(users.save(u));
    }

    /**
     * Admins may change name, email, role and password of any user.
     * Everyone else may only update their own name and password (role/email in the body are ignored).
     * Changing your own password always requires the current password. Any password change revokes the user's tokens.
     */
    @Transactional
    public User update(Long id, UpdateUserRequest r, UserPrincipal me) {
        User u = find(id);
        boolean self = u.getId().equals(me.getId());
        if (r.name() != null) {
            if (r.name().isBlank()) throw ApiException.badRequest("Name must not be blank");
            u.setName(r.name().trim());
        }
        if (me.isAdmin()) {
            if (r.email() != null && !r.email().isBlank()) {
                String email = r.email().trim().toLowerCase();
                users.findByEmailIgnoreCase(email).filter(o -> !o.getId().equals(id)).ifPresent(o -> { throw ApiException.conflict("Email is already in use"); });
                u.setEmail(email);
            }
            if (r.role() != null) {
                if (self && r.role() != Role.ADMIN) throw ApiException.badRequest("You cannot remove your own admin role");
                u.setRole(r.role());
            }
        }
        if (r.password() != null && !r.password().isBlank()) {
            if (self && (r.currentPassword() == null || !encoder.matches(r.currentPassword(), u.getPassword()))) {
                throw ApiException.badRequest("Current password is incorrect");
            }
            u.setPassword(encoder.encode(r.password()));
            u.revokeTokens(); // signs out every existing session of this user
        }
        return users.save(u);
    }

    @Transactional
    public void delete(Long id, UserPrincipal me) {
        if (id.equals(me.getId())) throw ApiException.badRequest("You cannot delete your own account");
        users.delete(find(id)); // related progress, attempts and projects are removed by ON DELETE CASCADE
    }

    private User find(Long id) { return users.findById(id).orElseThrow(() -> ApiException.notFound("User")); }
}
