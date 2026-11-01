package com.skillmate.controller;

import com.skillmate.dto.TrackingDtos.*;
import com.skillmate.security.UserPrincipal;
import com.skillmate.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projects;
    public ProjectController(ProjectService projects) { this.projects = projects; }

    @GetMapping("/me")
    public List<ProjectDto> mine(@AuthenticationPrincipal UserPrincipal me) { return projects.listMine(me.getId()); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto create(@Valid @RequestBody ProjectRequest r, @AuthenticationPrincipal UserPrincipal me) { return projects.create(me.getId(), r); }

    @PutMapping("/{id}")
    public ProjectDto update(@PathVariable Long id, @Valid @RequestBody ProjectRequest r, @AuthenticationPrincipal UserPrincipal me) { return projects.update(id, me.getId(), r); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal me) { projects.delete(id, me.getId()); }
}
