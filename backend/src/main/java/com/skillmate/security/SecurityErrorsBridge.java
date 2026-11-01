package com.skillmate.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class SecurityErrorsBridge {
    private SecurityErrorsBridge() {}
    public static void unauthorized(HttpServletResponse res) throws IOException {
        SecurityErrors.write(res, 401, "Unauthorized", "Authentication required or session expired");
    }
    public static void forbidden(HttpServletResponse res) throws IOException {
        SecurityErrors.write(res, 403, "Forbidden", "You do not have permission to perform this action");
    }
}
