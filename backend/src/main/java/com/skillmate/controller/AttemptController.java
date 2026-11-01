package com.skillmate.controller;

import com.skillmate.dto.TrackingDtos.*;
import com.skillmate.security.UserPrincipal;
import com.skillmate.service.AttemptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/attempts")
public class AttemptController {
    private final AttemptService attempts;
    public AttemptController(AttemptService attempts) { this.attempts = attempts; }

    @GetMapping("/me")
    public List<AttemptDto> mine(@RequestParam(defaultValue = "100") int limit, @AuthenticationPrincipal UserPrincipal me) {
        return attempts.listMine(me.getId(), limit); // newest first, at most AttemptService.MAX_LIST
    }

    @GetMapping("/me/question/{questionId}")
    public List<AttemptDto> forQuestion(@PathVariable Long questionId, @AuthenticationPrincipal UserPrincipal me) { return attempts.forQuestion(me.getId(), questionId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttemptDto record(@Valid @RequestBody AttemptRequest r, @AuthenticationPrincipal UserPrincipal me) { return attempts.record(me.getId(), r); }
}
