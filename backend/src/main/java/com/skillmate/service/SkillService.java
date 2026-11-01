package com.skillmate.service;

import com.skillmate.dto.CatalogDtos.*;
import com.skillmate.entity.*;
import com.skillmate.exception.ApiException;
import com.skillmate.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SkillService {
    private final SkillRepository skills; private final SubjectRepository subjects;
    private final QuestionRepository questions; private final ProgressRepository progress;
    public SkillService(SkillRepository skills, SubjectRepository subjects, QuestionRepository questions, ProgressRepository progress) {
        this.skills = skills; this.subjects = subjects; this.questions = questions; this.progress = progress;
    }

    public List<SkillDto> list(Long subjectId, Long userId) {
        if (subjectId != null && !subjects.existsById(subjectId)) throw ApiException.notFound("Subject");
        List<Skill> list = subjectId == null ? skills.findAllOrdered() : skills.findBySubjectIdOrderByNameAsc(subjectId);
        return toDtos(list, userId);
    }

    public SkillDto get(Long id, Long userId) { return toDtos(List.of(find(id)), userId).get(0); }

    @Transactional
    public SkillDto create(SkillRequest r, Long userId) {
        Subject subject = subjects.findById(r.subjectId()).orElseThrow(() -> ApiException.notFound("Subject"));
        String name = r.name().trim();
        if (skills.findBySubjectIdAndNameIgnoreCase(subject.getId(), name).isPresent()) throw ApiException.conflict("This subject already has a skill with that name");
        Skill s = new Skill();
        s.setName(name); s.setSubject(subject);
        return toDtos(List.of(skills.save(s)), userId).get(0);
    }

    @Transactional
    public SkillDto update(Long id, SkillRequest r, Long userId) {
        Skill s = find(id);
        Subject subject = subjects.findById(r.subjectId()).orElseThrow(() -> ApiException.notFound("Subject"));
        String name = r.name().trim();
        skills.findBySubjectIdAndNameIgnoreCase(subject.getId(), name).filter(o -> !o.getId().equals(id))
              .ifPresent(o -> { throw ApiException.conflict("This subject already has a skill with that name"); });
        s.setName(name); s.setSubject(subject);
        return toDtos(List.of(skills.save(s)), userId).get(0);
    }

    @Transactional
    public void delete(Long id) { skills.delete(find(id)); }

    /** Combines skills with the question count and the given user's progress (NOT_STARTED / 0% when no record). */
    public List<SkillDto> toDtos(List<Skill> list, Long userId) {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : questions.countGroupedBySkill()) counts.put((Long) row[0], ((Number) row[1]).longValue());
        Map<Long, Progress> pm = progress.findByUserId(userId).stream().collect(Collectors.toMap(p -> p.getSkill().getId(), p -> p));
        return list.stream().map(s -> {
            Progress p = pm.get(s.getId());
            return new SkillDto(s.getId(), s.getName(), s.getSubject().getId(), s.getSubject().getName(), counts.getOrDefault(s.getId(), 0L),
                    p == null ? null : p.getId(), p == null ? ProgressStatus.NOT_STARTED : p.getStatus(), p == null ? 0 : p.getPercentage().doubleValue());
        }).toList();
    }

    private Skill find(Long id) { return skills.findById(id).orElseThrow(() -> ApiException.notFound("Skill")); }
}
