package com.skillmate.controller;

import com.skillmate.dto.TrackingDtos.*;
import com.skillmate.security.UserPrincipal;
import com.skillmate.service.ProgressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** All progress endpoints act on the authenticated user only; ids in the path never select another user's data. */
@RestController
@RequestMapping("/api/progress")
public class ProgressController {
    private final ProgressService progress;
    public ProgressController(ProgressService progress) { this.progress = progress; }

    @GetMapping("/me")
    public List<ProgressDto> mine(@AuthenticationPrincipal UserPrincipal me) { return progress.listMine(me.getId()); }

    @GetMapping("/me/skills/{skillId}")
    public ProgressDto forSkill(@PathVariable Long skillId, @AuthenticationPrincipal UserPrincipal me) { return progress.forSkill(me.getId(), skillId); }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ProgressDto upsert(@Valid @RequestBody ProgressRequest r, @AuthenticationPrincipal UserPrincipal me) {
        return progress.upsert(me.getId(), r.skillId(), r.status());
    }

    @PutMapping("/{id}")
    public ProgressDto update(@PathVariable Long id, @Valid @RequestBody ProgressUpdateRequest r, @AuthenticationPrincipal UserPrincipal me) {
        return progress.update(id, me.getId(), r.status());
    }
}
