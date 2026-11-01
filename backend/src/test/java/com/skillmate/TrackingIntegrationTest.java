package com.skillmate;

import com.skillmate.entity.Question;
import com.skillmate.entity.Skill;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TrackingIntegrationTest extends AbstractIntegrationTest {

    @Test
    void progressUpsertDoesNotCreateDuplicates() throws Exception {
        Cookie a = registerStudent();
        Skill skill = newSkillWithQuestions(2);
        long id1 = body(mvc.perform(withBody(post("/api/progress"), a, Map.of("skillId", skill.getId(), "status", "IN_PROGRESS"))).andExpect(status().isOk()).andReturn()).get("id").asLong();
        MvcResult second = mvc.perform(withBody(post("/api/progress"), a, Map.of("skillId", skill.getId(), "status", "COMPLETED"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.percentage").value(100.0)).andReturn();
        assertThat(body(second).get("id").asLong()).isEqualTo(id1);
        mvc.perform(authed(get("/api/progress/me"), a)).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void studentCannotModifyAnotherStudentsProgress() throws Exception {
        Cookie a = registerStudent();
        Cookie b = registerStudent();
        Skill skill = newSkillWithQuestions(1);
        long progressId = body(mvc.perform(withBody(post("/api/progress"), a, Map.of("skillId", skill.getId(), "status", "IN_PROGRESS"))).andReturn()).get("id").asLong();
        mvc.perform(withBody(put("/api/progress/" + progressId), b, Map.of("status", "COMPLETED"))).andExpect(status().isNotFound());
        mvc.perform(authed(get("/api/progress/me"), b)).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(withBody(put("/api/progress/" + progressId), a, Map.of("status", "COMPLETED"))).andExpect(status().isOk());
    }

    @Test
    void solvingQuestionsDrivesProgressPercentage() throws Exception {
        Cookie a = registerStudent();
        Skill skill = newSkillWithQuestions(4);
        List<Question> qs = questions.findAll().stream().filter(q -> q.getSkill().getId().equals(skill.getId())).toList();
        mvc.perform(withBody(post("/api/attempts"), a, Map.of("questionId", qs.get(0).getId(), "status", "SOLVED"))).andExpect(status().isCreated());
        mvc.perform(authed(get("/api/progress/me/skills/" + skill.getId()), a))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS")).andExpect(jsonPath("$.percentage").value(25.0));
        mvc.perform(withBody(post("/api/attempts"), a, Map.of("questionId", qs.get(1).getId(), "status", "SOLVED"))).andExpect(status().isCreated());
        mvc.perform(authed(get("/api/progress/me/skills/" + skill.getId()), a)).andExpect(jsonPath("$.percentage").value(50.0));
    }

    @Test
    void openingOrAttemptingWithoutSolvingDoesNotMarkSolved() throws Exception {
        Cookie a = registerStudent();
        Skill skill = newSkillWithQuestions(2);
        Question q = questions.findAll().stream().filter(x -> x.getSkill().getId().equals(skill.getId())).findFirst().orElseThrow();
        mvc.perform(authed(get("/api/questions/" + q.getId()), a)).andExpect(jsonPath("$.myStatus").doesNotExist());
        mvc.perform(withBody(post("/api/attempts"), a, Map.of("questionId", q.getId(), "status", "ATTEMPTED"))).andExpect(status().isCreated());
        mvc.perform(authed(get("/api/questions/" + q.getId()), a)).andExpect(jsonPath("$.myStatus").value("ATTEMPTED"));
        mvc.perform(authed(get("/api/progress/me/skills/" + skill.getId()), a)).andExpect(jsonPath("$.percentage").value(1.0));
    }

    @Test
    void attemptsAreOwnedAndRetriesAreDeduplicated() throws Exception {
        Cookie a = registerStudent();
        Cookie b = registerStudent();
        Skill skill = newSkillWithQuestions(1);
        Question q = questions.findAll().stream().filter(x -> x.getSkill().getId().equals(skill.getId())).findFirst().orElseThrow();
        Map<String, Object> req = Map.of("questionId", q.getId(), "status", "SOLVED");
        mvc.perform(withBody(post("/api/attempts"), a, req)).andExpect(status().isCreated());
        mvc.perform(withBody(post("/api/attempts"), a, req)).andExpect(status().isCreated()); // retry within the dedup window
        mvc.perform(authed(get("/api/attempts/me"), a)).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(authed(get("/api/attempts/me"), b)).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(authed(get("/api/attempts/me/question/" + q.getId()), b)).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void invalidAttemptAndProgressInputIsRejected() throws Exception {
        Cookie a = registerStudent();
        mvc.perform(withBody(post("/api/attempts"), a, Map.of("questionId", 1, "status", "PERFECT"))).andExpect(status().isBadRequest());
        mvc.perform(withBody(post("/api/attempts"), a, Map.of("questionId", 999999, "status", "SOLVED"))).andExpect(status().isNotFound());
        mvc.perform(withBody(post("/api/progress"), a, Map.of("status", "COMPLETED"))).andExpect(status().isBadRequest());
    }

    @Test
    void dashboardUsesOnlyPersistedDataAndNotAssessedWhenEmpty() throws Exception {
        Cookie a = registerStudent();
        mvc.perform(authed(get("/api/dashboard/me/readiness"), a))
                .andExpect(status().isOk()).andExpect(jsonPath("$.assessed").value(false)).andExpect(jsonPath("$.level").value("NOT_ASSESSED"))
                .andExpect(jsonPath("$.weakSkills.length()").value(0));
        Skill skill = newSkillWithQuestions(1);
        mvc.perform(withBody(post("/api/progress"), a, Map.of("skillId", skill.getId(), "status", "COMPLETED"))).andExpect(status().isOk());
        mvc.perform(authed(get("/api/dashboard/me"), a)).andExpect(jsonPath("$.completedSkills").value(1)).andExpect(jsonPath("$.readinessAssessed").value(true));
        mvc.perform(authed(get("/api/dashboard/admin"), a)).andExpect(status().isForbidden());
    }
}
