package com.skillmate.controller;

import com.skillmate.dto.CatalogDtos.*;
import com.skillmate.security.UserPrincipal;
import com.skillmate.service.SkillService;
import com.skillmate.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {
    private final SubjectService subjects; private final SkillService skills;
    public SubjectController(SubjectService subjects, SkillService skills) { this.subjects = subjects; this.skills = skills; }

    @GetMapping
    public List<SubjectDto> list(@AuthenticationPrincipal UserPrincipal me) { return subjects.list(me.getId()); }

    @GetMapping("/{id}")
    public SubjectDto get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal me) { return subjects.get(id, me.getId()); }

    @GetMapping("/{subjectId}/skills")
    public List<SkillDto> skills(@PathVariable Long subjectId, @AuthenticationPrincipal UserPrincipal me) { return skills.list(subjectId, me.getId()); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public SubjectDto create(@Valid @RequestBody SubjectRequest r) { return subjects.create(r); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SubjectDto update(@PathVariable Long id, @Valid @RequestBody SubjectRequest r, @AuthenticationPrincipal UserPrincipal me) { return subjects.update(id, r, me.getId()); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { subjects.delete(id); }
}
