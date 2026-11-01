package com.skillmate.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

final class SecurityErrors {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private SecurityErrors() {}

    static void write(HttpServletResponse res, int status, String error, String message) throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        res.setStatus(status);
        res.setContentType("application/json");
        MAPPER.writeValue(res.getOutputStream(), body);
    }
}
