package com.skillmate.service;

import com.skillmate.dto.CatalogDtos.*;
import com.skillmate.entity.Progress;
import com.skillmate.entity.Skill;
import com.skillmate.entity.Subject;
import com.skillmate.exception.ApiException;
import com.skillmate.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SubjectService {
    private final SubjectRepository subjects; private final SkillRepository skills; private final ProgressRepository progress;
    public SubjectService(SubjectRepository subjects, SkillRepository skills, ProgressRepository progress) {
        this.subjects = subjects; this.skills = skills; this.progress = progress;
    }

    /** Subject progress = average percentage over all its skills; skills without a record count as 0%. */
    public List<SubjectDto> list(Long userId) {
        Map<Long, List<Skill>> bySubject = skills.findAll().stream().collect(Collectors.groupingBy(s -> s.getSubject().getId()));
        Map<Long, Progress> pm = progress.findByUserId(userId).stream().collect(Collectors.toMap(p -> p.getSkill().getId(), p -> p));
        return subjects.findAllByOrderByNameAsc().stream().map(s -> toDto(s, bySubject.getOrDefault(s.getId(), List.of()), pm)).toList();
    }

    public SubjectDto get(Long id, Long userId) {
        Subject s = find(id);
        Map<Long, Progress> pm = progress.findByUserId(userId).stream().collect(Collectors.toMap(p -> p.getSkill().getId(), p -> p));
        return toDto(s, skills.findBySubjectIdOrderByNameAsc(id), pm);
    }

    @Transactional
    public SubjectDto create(SubjectRequest r) {
        String name = r.name().trim();
        if (subjects.findByNameIgnoreCase(name).isPresent()) throw ApiException.conflict("A subject with this name already exists");
        Subject s = new Subject();
        s.setName(name); s.setDescription(blankToNull(r.description()));
        return toDto(subjects.save(s), List.of(), Map.of());
    }

    @Transactional
    public SubjectDto update(Long id, SubjectRequest r, Long userId) {
        Subject s = find(id);
        String name = r.name().trim();
        subjects.findByNameIgnoreCase(name).filter(o -> !o.getId().equals(id)).ifPresent(o -> { throw ApiException.conflict("A subject with this name already exists"); });
        s.setName(name); s.setDescription(blankToNull(r.description()));
        subjects.save(s);
        return get(id, userId); // includes the caller's own progress, like every other subject response
    }

    /** Deletes the subject and (via ON DELETE CASCADE) its skills, questions, progress and attempts. */
    @Transactional
    public void delete(Long id) { subjects.delete(find(id)); }

    private SubjectDto toDto(Subject s, List<Skill> list, Map<Long, Progress> pm) {
        double avg = list.isEmpty() ? 0 : list.stream().mapToDouble(k -> pm.containsKey(k.getId()) ? pm.get(k.getId()).getPercentage().doubleValue() : 0).average().orElse(0);
        return new SubjectDto(s.getId(), s.getName(), s.getDescription(), list.size(), Math.round(avg * 100.0) / 100.0);
    }

    private Subject find(Long id) { return subjects.findById(id).orElseThrow(() -> ApiException.notFound("Subject")); }
    private static String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}
