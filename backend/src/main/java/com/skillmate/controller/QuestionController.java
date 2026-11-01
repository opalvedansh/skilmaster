package com.skillmate.controller;

import com.skillmate.dto.AuthDtos.PageResponse;
import com.skillmate.dto.CatalogDtos.*;
import com.skillmate.entity.Difficulty;
import com.skillmate.security.UserPrincipal;
import com.skillmate.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    private final QuestionService questions;
    public QuestionController(QuestionService questions) { this.questions = questions; }

    @GetMapping
    public PageResponse<QuestionDto> list(@RequestParam(required = false) Long skillId, @RequestParam(required = false) Long subjectId,
                                          @RequestParam(required = false) Difficulty difficulty, @RequestParam(defaultValue = "") String q,
                                          @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                          @AuthenticationPrincipal UserPrincipal me) {
        return questions.search(skillId, subjectId, difficulty, q, page, size, me.getId());
    }

    @GetMapping("/{id}")
    public QuestionDto get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal me) { return questions.get(id, me.getId()); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionDto create(@Valid @RequestBody QuestionRequest r) { return questions.create(r); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public QuestionDto update(@PathVariable Long id, @Valid @RequestBody QuestionRequest r) { return questions.update(id, r); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { questions.delete(id); }
}
