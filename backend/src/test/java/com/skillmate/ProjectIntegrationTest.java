package com.skillmate;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectIntegrationTest extends AbstractIntegrationTest {
    private Map<String, Object> project(String title) {
        return Map.of("title", title, "description", "d", "technology", "React", "githubUrl", "https://github.com/me/repo", "status", "PLANNED");
    }

    @Test
    void ownerCanCreateUpdateListAndDelete() throws Exception {
        Cookie a = registerStudent();
        long id = body(mvc.perform(withBody(post("/api/projects"), a, project("Portfolio"))).andExpect(status().isCreated()).andReturn()).get("id").asLong();
        mvc.perform(withBody(put("/api/projects/" + id), a, Map.of("title", "Portfolio v2", "status", "COMPLETED"))).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));
        mvc.perform(authed(get("/api/projects/me"), a)).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].title").value("Portfolio v2"));
        mvc.perform(authed(delete("/api/projects/" + id), a)).andExpect(status().isNoContent());
        mvc.perform(authed(get("/api/projects/me"), a)).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void otherStudentsAndAdminsCannotTouchSomeoneElsesProject() throws Exception {
        Cookie a = registerStudent();
        Cookie b = registerStudent();
        Cookie admin = loginAdmin();
        long id = body(mvc.perform(withBody(post("/api/projects"), a, project("Private"))).andReturn()).get("id").asLong();
        for (Cookie other : new Cookie[]{b, admin}) {
            mvc.perform(withBody(put("/api/projects/" + id), other, project("Hijacked"))).andExpect(status().isNotFound());
            mvc.perform(authed(delete("/api/projects/" + id), other)).andExpect(status().isNotFound());
            mvc.perform(authed(get("/api/projects/me"), other)).andExpect(jsonPath("$.length()").value(0));
        }
        mvc.perform(authed(get("/api/projects/me"), a)).andExpect(jsonPath("$[0].title").value("Private"));
    }

    @Test
    void invalidProjectInputIsRejected() throws Exception {
        Cookie a = registerStudent();
        mvc.perform(withBody(post("/api/projects"), a, Map.of("title", "", "status", "PLANNED"))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.title").exists());
        mvc.perform(withBody(post("/api/projects"), a, Map.of("title", "X", "githubUrl", "http://evil.example.com/x", "status", "PLANNED")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.githubUrl").exists());
        mvc.perform(withBody(post("/api/projects"), a, Map.of("title", "X", "status", "DONE"))).andExpect(status().isBadRequest());
    }
}
