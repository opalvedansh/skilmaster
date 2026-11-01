package com.skillmate.controller;

import com.skillmate.dto.AuthDtos.*;
import com.skillmate.entity.User;
import com.skillmate.exception.ApiException;
import com.skillmate.repository.UserRepository;
import com.skillmate.security.JwtService;
import com.skillmate.security.UserPrincipal;
import com.skillmate.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth; private final JwtService jwt; private final UserRepository users;

    public AuthController(AuthService auth, JwtService jwt, UserRepository users) {
        this.auth = auth; this.jwt = jwt; this.users = users;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest r) {
        User u = auth.register(r);
        return ResponseEntity.status(HttpStatus.CREATED).header(HttpHeaders.SET_COOKIE, jwt.loginCookie(u).toString()).body(UserDto.from(u));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody LoginRequest r) {
        User u = auth.login(r);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwt.loginCookie(u).toString()).body(UserDto.from(u));
    }

    /** Revokes every token of the user (all devices), not only this cookie. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal me) {
        if (me != null) auth.logout(me.getId());
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, jwt.logoutCookie().toString()).build();
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal UserPrincipal me) {
        return users.findById(me.getId()).map(UserDto::from).orElseThrow(() -> ApiException.unauthorized("Session expired"));
    }
}
