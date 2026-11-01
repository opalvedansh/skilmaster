package com.skillmate;

import com.skillmate.entity.Skill;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CatalogCrudIntegrationTest extends AbstractIntegrationTest {

    @Test
    void studentCannotWriteCatalogButCanRead() throws Exception {
        Cookie student = registerStudent();

        mvc.perform(withBody(post("/api/subjects"), student, Map.of("name", "Hack")))
                .andExpect(status().isForbidden());

        mvc.perform(withBody(post("/api/skills"), student, Map.of("name", "Hack", "subjectId", 1)))
                .andExpect(status().isForbidden());

        mvc.perform(withBody(post("/api/questions"), student, Map.of("questionText", "x", "difficulty", "EASY", "skillId", 1)))
                .andExpect(status().isForbidden());

        mvc.perform(authed(delete("/api/subjects/1"), student))
                .andExpect(status().isForbidden());

        mvc.perform(authed(get("/api/users"), student))
                .andExpect(status().isForbidden());

        mvc.perform(authed(get("/api/subjects"), student))
                .andExpect(status().isOk());
    }

    @Test
    void adminSubjectCrud() throws Exception {
        Cookie admin = loginAdmin();

        MvcResult created = mvc.perform(withBody(
                        post("/api/subjects"), admin,
                        Map.of("name", "Compilers", "description", "Parsing")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Compilers"))
                .andReturn();

        long id = body(created).get("id").asLong();

        mvc.perform(authed(get("/api/subjects/" + id), admin))
                .andExpect(status().isOk());

        mvc.perform(withBody(
                        put("/api/subjects/" + id), admin,
                        Map.of("name", "Compiler Design")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Compiler Design"));

        mvc.perform(withBody(
                        post("/api/subjects"), admin,
                        Map.of("name", "compiler design")))
                .andExpect(status().isConflict());

        mvc.perform(authed(delete("/api/subjects/" + id), admin))
                .andExpect(status().isNoContent());

        mvc.perform(authed(get("/api/subjects/" + id), admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminSkillCrud() throws Exception {
        Cookie admin = loginAdmin();
        Skill base = newSkillWithQuestions(0);
        long subjectId = base.getSubject().getId();

        MvcResult created = mvc.perform(withBody(
                        post("/api/skills"), admin,
                        Map.of("name", "Lexing", "subjectId", subjectId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subjectId").value(subjectId))
                .andReturn();

        long id = body(created).get("id").asLong();

        mvc.perform(withBody(
                        put("/api/skills/" + id), admin,
                        Map.of("name", "Lexical Analysis", "subjectId", subjectId)))
                .andExpect(status().isOk());

        mvc.perform(authed(get("/api/subjects/" + subjectId + "/skills"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mvc.perform(withBody(
                        post("/api/skills"), admin,
                        Map.of("name", "Lexical Analysis", "subjectId", subjectId)))
                .andExpect(status().isConflict());

        mvc.perform(withBody(
                        post("/api/skills"), admin,
                        Map.of("name", "Orphan", "subjectId", 999999)))
                .andExpect(status().isNotFound());

        mvc.perform(authed(delete("/api/skills/" + id), admin))
                .andExpect(status().isNoContent());

        mvc.perform(authed(get("/api/skills/" + id), admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminQuestionCrudAndFiltering() throws Exception {
        Cookie admin = loginAdmin();
        Skill skill = newSkillWithQuestions(0);

        MvcResult created = mvc.perform(withBody(
                        post("/api/questions"), admin,
                        Map.of("questionText", "What is a token?", "difficulty", "HARD", "skillId", skill.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.difficulty").value("HARD"))
                .andReturn();

        long id = body(created).get("id").asLong();

        mvc.perform(authed(get("/api/skills/" + skill.getId() + "/questions?difficulty=HARD"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mvc.perform(authed(get("/api/skills/" + skill.getId() + "/questions?difficulty=EASY"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mvc.perform(authed(get("/api/questions?skillId=" + skill.getId() + "&q=token"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mvc.perform(withBody(
                        put("/api/questions/" + id), admin,
                        Map.of("questionText", "Define token", "difficulty", "EASY", "skillId", skill.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.difficulty").value("EASY"));

        mvc.perform(authed(delete("/api/questions/" + id), admin))
                .andExpect(status().isNoContent());

        mvc.perform(authed(get("/api/questions/" + id), admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidInputIsRejectedWith400() throws Exception {
        Cookie admin = loginAdmin();
        Skill skill = newSkillWithQuestions(0);

        mvc.perform(withBody(
                        post("/api/questions"), admin,
                        Map.of("questionText", " ", "difficulty", "EASY", "skillId", skill.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.questionText").exists());

        mvc.perform(withBody(
                        post("/api/questions"), admin,
                        Map.of("questionText", "ok", "difficulty", "IMPOSSIBLE", "skillId", skill.getId())))
                .andExpect(status().isBadRequest());

        mvc.perform(withBody(post("/api/subjects"), admin, Map.of("name", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usersEndpointsAreAdminOnlyExceptSelf() throws Exception {
        Cookie student = registerStudent();
        Cookie admin = loginAdmin();

        mvc.perform(authed(get("/api/users"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());

        long selfId = body(
                mvc.perform(authed(get("/api/auth/me"), student)).andReturn()
        ).get("id").asLong();

        mvc.perform(authed(get("/api/users/" + selfId), student))
                .andExpect(status().isOk());

        mvc.perform(withBody(
                        put("/api/users/" + selfId), student,
                        Map.of("name", "New Name", "role", "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.role").value("STUDENT"));

        mvc.perform(authed(get("/api/users/" + (selfId + 1000)), student))
                .andExpect(status().isForbidden());

        mvc.perform(authed(delete("/api/users/" + selfId), student))
                .andExpect(status().isForbidden());
    }
}