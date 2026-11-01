package com.skillmate.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.skillmate.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

/** Reads the JWT from the HttpOnly cookie, validates it and loads the current user from the database. */
public class JwtAuthFilter extends OncePerRequestFilter {
    public static final String COOKIE_NAME = "sm_token";
    private final JwtService jwtService;
    private final UserRepository users;

    public JwtAuthFilter(JwtService jwtService, UserRepository users) { this.jwtService = jwtService; this.users = users; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String token = null;
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) if (COOKIE_NAME.equals(c.getName())) token = c.getValue();
        }
        if (token != null && !token.isBlank()) {
            try {
                Claims claims = jwtService.parse(token);
                users.findById(Long.valueOf(claims.getSubject()))
                        // tokens issued before the last logout or password change are rejected
                        .filter(u -> claims.get(JwtService.VERSION_CLAIM) instanceof Number v && v.longValue() == u.getTokenVersion())
                        .ifPresent(u -> {
                    UserPrincipal p = new UserPrincipal(u.getId(), u.getEmail(), u.getRole());
                    var auth = new UsernamePasswordAuthenticationToken(p, null, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext(); // expired/invalid token => treated as anonymous => 401
            }
        }
        chain.doFilter(req, res);
    }
}
