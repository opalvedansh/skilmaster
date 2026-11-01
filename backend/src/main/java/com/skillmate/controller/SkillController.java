package com.skillmate.controller;

import com.skillmate.dto.CatalogDtos.*;
import com.skillmate.dto.AuthDtos.PageResponse;
import com.skillmate.entity.Difficulty;
import com.skillmate.security.UserPrincipal;
import com.skillmate.service.QuestionService;
import com.skillmate.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class SkillController {
    private final SkillService skills; private final QuestionService questions;
    public SkillController(SkillService skills, QuestionService questions) { this.skills = skills; this.questions = questions; }

    @GetMapping("/api/skills")
    public List<SkillDto> list(@RequestParam(required = false) Long subjectId, @AuthenticationPrincipal UserPrincipal me) { return skills.list(subjectId, me.getId()); }

    @GetMapping("/api/skills/{id}")
    public SkillDto get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal me) { return skills.get(id, me.getId()); }

    @GetMapping("/api/skills/{skillId}/questions")
    public PageResponse<QuestionDto> questions(@PathVariable Long skillId, @RequestParam(required = false) Difficulty difficulty,
                                               @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size,
                                               @AuthenticationPrincipal UserPrincipal me) {
        skills.get(skillId, me.getId()); // 404 if the skill does not exist
        return questions.search(skillId, null, difficulty, "", page, size, me.getId());
    }

    @PostMapping("/api/skills")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public SkillDto create(@Valid @RequestBody SkillRequest r, @AuthenticationPrincipal UserPrincipal me) { return skills.create(r, me.getId()); }

    @PutMapping("/api/skills/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SkillDto update(@PathVariable Long id, @Valid @RequestBody SkillRequest r, @AuthenticationPrincipal UserPrincipal me) { return skills.update(id, r, me.getId()); }

    @DeleteMapping("/api/skills/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { skills.delete(id); }
}
