package com.skillmate.service;

import com.skillmate.dto.AuthDtos.*;
import com.skillmate.entity.Role;
import com.skillmate.entity.User;
import com.skillmate.exception.ApiException;
import com.skillmate.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    public static final int MAX_FAILED_LOGINS = 5;
    public static final long LOCKOUT_MILLIS = 15 * 60 * 1000L;

    private final UserRepository users; private final PasswordEncoder encoder;
    /** Hash checked when the email is unknown, so both cases take the same BCrypt time. */
    private final String dummyHash;
    /** email -> {failed count, window start millis}. shortcut: in-memory and per instance, use Redis or a DB table when running several instances. */
    private final Map<String, long[]> failures = new ConcurrentHashMap<>();

    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users; this.encoder = encoder; this.dummyHash = encoder.encode("timing-equaliser-not-a-password");
    }

    /** Public registration always creates a STUDENT; any role supplied by the client is ignored. */
    @Transactional
    public User register(RegisterRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) throw ApiException.conflict("An account with this email already exists");
        User u = new User();
        u.setName(r.name().trim()); u.setEmail(email); u.setPassword(encoder.encode(r.password())); u.setRole(Role.STUDENT);
        return users.save(u);
    }

    /** After MAX_FAILED_LOGINS wrong passwords for one email, further logins are refused for LOCKOUT_MILLIS. */
    @Transactional(readOnly = true)
    public User login(LoginRequest r) {
        String email = r.email().trim().toLowerCase();
        long now = System.currentTimeMillis();
        long[] f = failures.get(email);
        if (f != null && now - f[1] < LOCKOUT_MILLIS && f[0] >= MAX_FAILED_LOGINS) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Too many failed login attempts. Try again in 15 minutes.");
        }
        User u = users.findByEmailIgnoreCase(email).orElse(null);
        boolean matches = encoder.matches(r.password(), u == null ? dummyHash : u.getPassword());
        if (u == null || !matches) {
            recordFailure(email, now);
            throw ApiException.unauthorized("Invalid email or password");
        }
        failures.remove(email);
        return u;
    }

    private void recordFailure(String email, long now) {
        if (failures.size() > 10_000) failures.values().removeIf(v -> now - v[1] >= LOCKOUT_MILLIS); // bound memory under email spraying
        failures.compute(email, (k, v) -> v == null || now - v[1] >= LOCKOUT_MILLIS ? new long[]{1, now} : new long[]{v[0] + 1, v[1]});
    }

    @Transactional
    public void logout(Long userId) {
        users.findById(userId).ifPresent(User::revokeTokens);
    }
}
