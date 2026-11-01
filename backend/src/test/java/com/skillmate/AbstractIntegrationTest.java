package com.skillmate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillmate.entity.*;
import com.skillmate.repository.*;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** Each test runs in a transaction that is rolled back, so tests do not leak data into each other. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {
    protected static final String PASSWORD = "Passw0rd!x";
    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper om;
    @Autowired protected UserRepository users;
    @Autowired protected SubjectRepository subjects;
    @Autowired protected SkillRepository skills;
    @Autowired protected QuestionRepository questions;
    @Autowired protected PasswordEncoder encoder;

    protected String json(Object o) throws Exception { return om.writeValueAsString(o); }
    protected String uniqueEmail() { return "u" + UUID.randomUUID().toString().substring(0, 8) + "@example.com"; }

    protected MockHttpServletRequestBuilder withBody(MockHttpServletRequestBuilder b, Cookie c, Object body) throws Exception {
        b.header("X-Requested-With", "test").contentType(MediaType.APPLICATION_JSON).content(json(body));
        return c == null ? b : b.cookie(c);
    }
    protected MockHttpServletRequestBuilder authed(MockHttpServletRequestBuilder b, Cookie c) {
        b.header("X-Requested-With", "test");
        return c == null ? b : b.cookie(c);
    }

    protected Cookie tokenFrom(MvcResult r) {
        String header = r.getResponse().getHeader("Set-Cookie");
        String value = header.substring(header.indexOf('=') + 1, header.indexOf(';'));
        return new Cookie("sm_token", value);
    }

    protected Cookie registerStudent() throws Exception { return registerStudent(uniqueEmail()); }

    protected Cookie registerStudent(String email) throws Exception {
        MvcResult r = mvc.perform(withBody(post("/api/auth/register"), null, Map.of("name", "Test Student", "email", email, "password", PASSWORD)))
                .andReturn();
        if (r.getResponse().getStatus() != 201) throw new IllegalStateException("register failed: " + r.getResponse().getContentAsString());
        return tokenFrom(r);
    }

    protected Cookie loginAdmin() throws Exception {
        String email = uniqueEmail();
        User admin = new User();
        admin.setName("Admin"); admin.setEmail(email); admin.setPassword(encoder.encode(PASSWORD)); admin.setRole(Role.ADMIN);
        users.save(admin);
        MvcResult r = mvc.perform(withBody(post("/api/auth/login"), null, Map.of("email", email, "password", PASSWORD))).andReturn();
        return tokenFrom(r);
    }

    protected JsonNode body(MvcResult r) throws Exception { return om.readTree(r.getResponse().getContentAsString()); }

    protected Skill newSkillWithQuestions(int questionCount) {
        Subject s = new Subject(); s.setName("Test Subject " + UUID.randomUUID()); subjects.save(s);
        Skill k = new Skill(); k.setName("Test Skill"); k.setSubject(s); skills.save(k);
        for (int i = 0; i < questionCount; i++) {
            Question q = new Question(); q.setQuestionText("Question " + i); q.setDifficulty(Difficulty.EASY); q.setSkill(k); questions.save(q);
        }
        return k;
    }
}
