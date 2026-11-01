package com.skillmate;

import com.skillmate.entity.Question;
import com.skillmate.entity.Skill;
import com.skillmate.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import java.util.List;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Regression tests for the backend scan fixes (token revocation, 4xx mapping, stale progress, search, lockout). */
class FixesIntegrationTest extends AbstractIntegrationTest {

    @Test
    void logoutRevokesTheOldToken() throws Exception {
        Cookie c = registerStudent();
        mvc.perform(authed(get("/api/auth/me"), c)).andExpect(status().isOk());
        mvc.perform(authed(post("/api/auth/logout"), c)).andExpect(status().isNoContent());
        mvc.perform(authed(get("/api/auth/me"), c)).andExpect(status().isUnauthorized());
    }

    @Test
    void passwordChangeRevokesOldTokenAndIssuesNewOne() throws Exception {
        Cookie old = registerStudent();
        long id = body(mvc.perform(authed(get("/api/auth/me"), old)).andReturn()).get("id").asLong();
        MvcResult r = mvc.perform(withBody(put("/api/users/" + id), old, Map.of("currentPassword", PASSWORD, "password", "NewPassw0rd")))
                .andExpect(status().isOk()).andExpect(header().exists("Set-Cookie")).andReturn();
        mvc.perform(authed(get("/api/auth/me"), old)).andExpect(status().isUnauthorized());
        mvc.perform(authed(get("/api/auth/me"), tokenFrom(r))).andExpect(status().isOk());
    }

    @Test
    void wrongContentTypeIs415NotServerError() throws Exception {
        mvc.perform(post("/api/auth/login").header("X-Requested-With", "t").contentType(MediaType.TEXT_PLAIN).content("hi"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void searchTreatsPercentAndUnderscoreLiterally() throws Exception {
        Cookie c = registerStudent();
        mvc.perform(authed(get("/api/questions").param("q", "_"), c)).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(authed(get("/api/questions").param("q", "%"), c)).andExpect(jsonPath("$.totalElements").value(0));
        Cookie admin = loginAdmin();
        mvc.perform(authed(get("/api/users").param("q", "_"), admin)).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void questionChangesRecalculateInProgressPercentage() throws Exception {
        Cookie student = registerStudent();
        Cookie admin = loginAdmin();
        Skill skill = newSkillWithQuestions(0);
        String progressUrl = "/api/progress/me/skills/" + skill.getId();
        mvc.perform(withBody(post("/api/progress"), student, Map.of("skillId", skill.getId(), "status", "IN_PROGRESS")));
        mvc.perform(authed(get(progressUrl), student)).andExpect(jsonPath("$.percentage").value(50.0)); // no questions yet

        Map<String, Object> q = Map.of("questionText", "Q", "difficulty", "EASY", "skillId", skill.getId());
        long q1 = body(mvc.perform(withBody(post("/api/questions"), admin, q)).andExpect(status().isCreated()).andReturn()).get("id").asLong();
        mvc.perform(withBody(post("/api/questions"), admin, q)).andExpect(status().isCreated());
        mvc.perform(authed(get(progressUrl), student)).andExpect(jsonPath("$.percentage").value(1.0)); // 0 of 2 solved

        mvc.perform(withBody(post("/api/attempts"), student, Map.of("questionId", q1, "status", "SOLVED")));
        mvc.perform(authed(get(progressUrl), student)).andExpect(jsonPath("$.percentage").value(50.0)); // 1 of 2

        mvc.perform(authed(delete("/api/questions/" + q1), admin)).andExpect(status().isNoContent());
        mvc.perform(authed(get(progressUrl), student)).andExpect(jsonPath("$.percentage").value(1.0)); // solved one is gone, 0 of 1
    }

    @Test
    void loginLocksAfterRepeatedFailures() throws Exception {
        String email = uniqueEmail();
        registerStudent(email);
        for (int i = 0; i < AuthService.MAX_FAILED_LOGINS; i++) {
            mvc.perform(withBody(post("/api/auth/login"), null, Map.of("email", email, "password", "wrongPass1"))).andExpect(status().isUnauthorized());
        }
        mvc.perform(withBody(post("/api/auth/login"), null, Map.of("email", email, "password", PASSWORD))).andExpect(status().isTooManyRequests());
    }

    @Test
    void unknownEmailIsRejectedEvenWithTheTimingDummyPassword() throws Exception {
        mvc.perform(withBody(post("/api/auth/login"), null, Map.of("email", uniqueEmail(), "password", "timing-equaliser-not-a-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void attemptListHonoursLimit() throws Exception {
        Cookie c = registerStudent();
        Skill skill = newSkillWithQuestions(2);
        List<Question> qs = questions.findAll().stream().filter(q -> q.getSkill().getId().equals(skill.getId())).toList();
        for (Question q : qs) mvc.perform(withBody(post("/api/attempts"), c, Map.of("questionId", q.getId(), "status", "ATTEMPTED")));
        mvc.perform(authed(get("/api/attempts/me"), c)).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(authed(get("/api/attempts/me").param("limit", "1"), c)).andExpect(jsonPath("$.length()").value(1));
    }
}
