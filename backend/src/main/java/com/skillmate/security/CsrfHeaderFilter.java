package com.skillmate.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Set;

/**
 * CSRF defence for the cookie-based session: every state-changing request must carry a custom header.
 * Browsers cannot add it cross-origin without a CORS preflight, which only allowed origins pass.
 * Combined with SameSite=Lax cookies this blocks cross-site request forgery.
 */
public class CsrfHeaderFilter extends OncePerRequestFilter {
    private static final Set<String> SAFE = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        if (!SAFE.contains(req.getMethod()) && req.getHeader("X-Requested-With") == null) {
            SecurityErrors.write(res, 403, "Forbidden", "Missing X-Requested-With header");
            return;
        }
        chain.doFilter(req, res);
    }
}
