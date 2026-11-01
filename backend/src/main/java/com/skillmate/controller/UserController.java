package com.skillmate.controller;

import com.skillmate.dto.AuthDtos.*;
import com.skillmate.entity.Role;
import com.skillmate.entity.User;
import com.skillmate.security.JwtService;
import com.skillmate.security.UserPrincipal;
import com.skillmate.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService users; private final JwtService jwt;
    public UserController(UserService users, JwtService jwt) { this.users = users; this.jwt = jwt; }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserDto> list(@RequestParam(defaultValue = "") String q, @RequestParam(required = false) Role role,
                                      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return users.list(q, role, page, size);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody CreateUserRequest r) { return users.create(r); }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public UserDto get(@PathVariable Long id) { return users.get(id); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserDto> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest r, @AuthenticationPrincipal UserPrincipal me) {
        User u = users.update(id, r, me);
        var res = ResponseEntity.ok();
        // the password change revoked the caller's own token, so hand them a fresh one
        if (id.equals(me.getId()) && r.password() != null && !r.password().isBlank()) res.header(HttpHeaders.SET_COOKIE, jwt.loginCookie(u).toString());
        return res.body(UserDto.from(u));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal me) { users.delete(id, me); }
}
