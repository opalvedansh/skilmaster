package com.skillmate;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void registerCreatesStudentAndNeverExposesPassword() throws Exception {
        MvcResult r = mvc.perform(withBody(post("/api/auth/register"), null,
                Map.of("name", "Ada", "email", "ada@example.com", "password", PASSWORD, "role", "ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();
        assertThat(r.getResponse().getHeader("Set-Cookie")).contains("HttpOnly").contains("SameSite=Lax");
        assertThat(users.findByEmailIgnoreCase("ada@example.com").orElseThrow().getPassword()).startsWith("$2"); // BCrypt hash
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        String email = uniqueEmail();
        registerStudent(email);
        mvc.perform(withBody(post("/api/auth/register"), null, Map.of("name", "Dup", "email", email.toUpperCase(), "password", PASSWORD)))
                .andExpect(status().isConflict());
    }

    @Test
    void registerValidatesInput() throws Exception {
        mvc.perform(withBody(post("/api/auth/register"), null, Map.of("name", "", "email", "not-an-email", "password", "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void loginSetsHttpOnlyCookieAndMeWorks() throws Exception {
        String email = uniqueEmail();
        registerStudent(email);
        MvcResult r = mvc.perform(withBody(post("/api/auth/login"), null, Map.of("email", email, "password", PASSWORD)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email)).andReturn();
        assertThat(r.getResponse().getHeader("Set-Cookie")).contains("sm_token=").contains("HttpOnly");
        mvc.perform(authed(get("/api/auth/me"), tokenFrom(r))).andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        String email = uniqueEmail();
        registerStudent(email);
        mvc.perform(withBody(post("/api/auth/login"), null, Map.of("email", email, "password", "Wrong-pass1")))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void protectedEndpointsRejectMissingOrInvalidToken() throws Exception {
        mvc.perform(authed(get("/api/subjects"), null)).andExpect(status().isUnauthorized());
        mvc.perform(authed(get("/api/subjects"), new Cookie("sm_token", "garbage.token.value"))).andExpect(status().isUnauthorized());
    }

    @Test
    void stateChangingRequestWithoutCsrfHeaderIsForbidden() throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json").content("{\"email\":\"a@b.co\",\"password\":\"x\"}"))
                .andExpect(status().isForbidden());
    }
}
