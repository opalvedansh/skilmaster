package com.skillmate.service;

import com.skillmate.dto.TrackingDtos.*;
import com.skillmate.entity.Project;
import com.skillmate.exception.ApiException;
import com.skillmate.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ProjectService {
    private final ProjectRepository projects; private final UserRepository users;
    public ProjectService(ProjectRepository projects, UserRepository users) { this.projects = projects; this.users = users; }

    @Transactional(readOnly = true)
    public List<ProjectDto> listMine(Long userId) { return projects.findByUserIdOrderByIdDesc(userId).stream().map(ProjectDto::from).toList(); }

    public ProjectDto create(Long userId, ProjectRequest r) {
        Project p = new Project();
        p.setUser(users.getReferenceById(userId));
        apply(p, r);
        return ProjectDto.from(projects.save(p));
    }

    public ProjectDto update(Long id, Long userId, ProjectRequest r) {
        Project p = owned(id, userId);
        apply(p, r);
        return ProjectDto.from(projects.save(p));
    }

    public void delete(Long id, Long userId) { projects.delete(owned(id, userId)); }

    /** Non-owners get 404 so project ids of other students are not revealed. */
    private Project owned(Long id, Long userId) {
        return projects.findById(id).filter(p -> p.getUser().getId().equals(userId)).orElseThrow(() -> ApiException.notFound("Project"));
    }

    private void apply(Project p, ProjectRequest r) {
        p.setTitle(r.title().trim());
        p.setDescription(blank(r.description()));
        p.setTechnology(blank(r.technology()));
        p.setGithubUrl(blank(r.githubUrl()));
        p.setStatus(r.status());
    }
    private static String blank(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
