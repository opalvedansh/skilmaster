package com.skillmate.service;

import com.skillmate.dto.TrackingDtos.ProgressDto;
import com.skillmate.entity.*;
import com.skillmate.exception.ApiException;
import com.skillmate.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Progress rules (one record per user + skill):
 *  - NOT_STARTED  => 0%
 *  - COMPLETED    => 100%
 *  - IN_PROGRESS  => share of the skill's questions the student has SOLVED, kept within 1..99%
 *                    (50% when the skill has no questions yet, because nothing can be measured).
 * Recording an attempt moves a NOT_STARTED skill to IN_PROGRESS and recalculates unless the skill is COMPLETED.
 * Adding, moving or deleting a question recalculates every IN_PROGRESS record of the affected skill.
 */
@Service
@Transactional
public class ProgressService {
    private final ProgressRepository progress; private final SkillRepository skills; private final UserRepository users;
    private final QuestionRepository questions; private final AttemptRepository attempts;

    public ProgressService(ProgressRepository progress, SkillRepository skills, UserRepository users,
                           QuestionRepository questions, AttemptRepository attempts) {
        this.progress = progress; this.skills = skills; this.users = users; this.questions = questions; this.attempts = attempts;
    }

    public static BigDecimal computePercentage(ProgressStatus status, long totalQuestions, long solvedQuestions) {
        double pct = switch (status) {
            case NOT_STARTED -> 0;
            case COMPLETED -> 100;
            case IN_PROGRESS -> totalQuestions == 0 ? 50 : Math.min(99, Math.max(1, 100.0 * solvedQuestions / totalQuestions));
        };
        return BigDecimal.valueOf(pct).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public List<ProgressDto> listMine(Long userId) {
        return progress.findByUserId(userId).stream().map(ProgressDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ProgressDto forSkill(Long userId, Long skillId) {
        Skill s = skills.findById(skillId).orElseThrow(() -> ApiException.notFound("Skill"));
        return progress.findByUserIdAndSkillId(userId, skillId).map(ProgressDto::from)
                .orElseGet(() -> new ProgressDto(null, s.getId(), s.getName(), s.getSubject().getId(), s.getSubject().getName(), ProgressStatus.NOT_STARTED, 0));
    }

    /** Creates or updates the single progress record for (user, skill). */
    public ProgressDto upsert(Long userId, Long skillId, ProgressStatus status) {
        Skill skill = skills.findById(skillId).orElseThrow(() -> ApiException.notFound("Skill"));
        User user = users.lockById(userId).orElseThrow(() -> ApiException.notFound("User")); // two concurrent upserts must not both insert
        Progress p = progress.findByUserIdAndSkillId(userId, skillId).orElseGet(() -> {
            Progress n = new Progress();
            n.setUser(user); n.setSkill(skill);
            return n;
        });
        p.setStatus(status);
        p.setPercentage(recalc(userId, skill, status));
        return ProgressDto.from(progress.save(p));
    }

    public ProgressDto update(Long progressId, Long userId, ProgressStatus status) {
        Progress p = progress.findById(progressId).filter(x -> x.getUser().getId().equals(userId))
                .orElseThrow(() -> ApiException.notFound("Progress record")); // 404 also hides other users' records
        p.setStatus(status);
        p.setPercentage(recalc(userId, p.getSkill(), status));
        return ProgressDto.from(progress.save(p));
    }

    /** Re-derives IN_PROGRESS percentages after questions of the skill were added, moved or deleted. */
    public void recalcSkill(Long skillId) {
        for (Progress p : progress.findBySkillIdAndStatus(skillId, ProgressStatus.IN_PROGRESS)) {
            p.setPercentage(recalc(p.getUser().getId(), p.getSkill(), ProgressStatus.IN_PROGRESS));
        }
    }

    /** Called by AttemptService inside the same transaction (holding the user lock) after an attempt is stored. */
    public void onAttempt(Long userId, Question question) {
        Skill skill = question.getSkill();
        Progress p = progress.findByUserIdAndSkillId(userId, skill.getId()).orElseGet(() -> {
            Progress n = new Progress();
            n.setUser(users.getReferenceById(userId)); n.setSkill(skill); n.setStatus(ProgressStatus.NOT_STARTED);
            return n;
        });
        if (p.getStatus() == ProgressStatus.NOT_STARTED) p.setStatus(ProgressStatus.IN_PROGRESS);
        p.setPercentage(recalc(userId, skill, p.getStatus()));
        progress.save(p);
    }

    private BigDecimal recalc(Long userId, Skill skill, ProgressStatus status) {
        if (status != ProgressStatus.IN_PROGRESS) return computePercentage(status, 0, 0);
        long total = questions.countBySkillId(skill.getId());
        long solved = attempts.countDistinctQuestionsInSkill(userId, AttemptStatus.SOLVED, skill.getId());
        return computePercentage(status, total, solved);
    }
}
