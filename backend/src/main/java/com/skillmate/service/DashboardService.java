package com.skillmate.service;

import com.skillmate.dto.CatalogDtos.SkillDto;
import com.skillmate.dto.DashboardDtos.*;
import com.skillmate.dto.TrackingDtos.AttemptDto;
import com.skillmate.entity.*;
import com.skillmate.exception.ApiException;
import com.skillmate.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {
    private final SkillService skillService; private final ReadinessCalculator calculator;
    private final SubjectRepository subjects; private final SkillRepository skills; private final QuestionRepository questions;
    private final AttemptRepository attempts; private final UserRepository users;

    public DashboardService(SkillService skillService, ReadinessCalculator calculator, SubjectRepository subjects, SkillRepository skills,
                            QuestionRepository questions, AttemptRepository attempts, UserRepository users) {
        this.skillService = skillService; this.calculator = calculator; this.subjects = subjects; this.skills = skills;
        this.questions = questions; this.attempts = attempts; this.users = users;
    }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
    private static double overall(List<SkillDto> list) { return list.isEmpty() ? 0 : round2(list.stream().mapToDouble(SkillDto::percentage).average().orElse(0)); }

    private List<ReadinessCalculator.SkillSnapshot> snapshots(List<SkillDto> list) {
        return list.stream().map(s -> new ReadinessCalculator.SkillSnapshot(s.id(), s.name(), s.subjectId(), s.subjectName(), s.assessed(), s.percentage())).toList();
    }

    public DashboardSummary me(Long userId) {
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("User"));
        List<SkillDto> list = skillService.list(null, userId);
        ReadinessDto readiness = calculator.calculate(snapshots(list));
        List<AttemptDto> recent = attempts.findTop10ByUserIdOrderByAttemptedAtDescIdDesc(userId).stream().map(AttemptDto::from).toList();
        return new DashboardSummary(user.getName(), subjects.count(), list.size(),
                list.stream().filter(s -> s.status() == ProgressStatus.COMPLETED).count(),
                list.stream().filter(s -> s.status() == ProgressStatus.IN_PROGRESS).count(),
                attempts.countDistinctQuestions(userId), attempts.countDistinctQuestionsByStatus(userId, AttemptStatus.SOLVED),
                overall(list), readiness.overallPercentage(), readiness.level(), readiness.assessed(), recent,
                readiness.strongSkills().stream().limit(5).toList(), readiness.weakSkills().stream().limit(5).toList());
    }

    public ProgressOverview progress(Long userId) {
        List<SkillDto> list = skillService.list(null, userId);
        Map<Long, List<SkillDto>> grouped = list.stream().collect(Collectors.groupingBy(SkillDto::subjectId, LinkedHashMap::new, Collectors.toList()));
        List<SubjectProgress> subjectProgress = grouped.values().stream().map(g -> new SubjectProgress(g.get(0).subjectId(), g.get(0).subjectName(), g.size(),
                (int) g.stream().filter(s -> s.status() == ProgressStatus.COMPLETED).count(),
                (int) g.stream().filter(s -> s.status() == ProgressStatus.IN_PROGRESS).count(), overall(g)))
                .sorted(Comparator.comparing(SubjectProgress::subjectName)).toList();
        int completed = (int) list.stream().filter(s -> s.status() == ProgressStatus.COMPLETED).count();
        int inProgress = (int) list.stream().filter(s -> s.status() == ProgressStatus.IN_PROGRESS).count();
        long totalQuestions = questions.count();
        long solved = attempts.countDistinctQuestionsByStatus(userId, AttemptStatus.SOLVED);
        AttemptStats stats = new AttemptStats(attempts.countByUserId(userId), attempts.countDistinctQuestions(userId), solved, totalQuestions,
                totalQuestions == 0 ? 0 : round2(100.0 * solved / totalQuestions));
        return new ProgressOverview(overall(list), completed, inProgress, list.size() - completed - inProgress, subjectProgress, list, stats);
    }

    public ReadinessDto readiness(Long userId) { return calculator.calculate(snapshots(skillService.list(null, userId))); }

    public AdminStats admin() {
        return new AdminStats(users.count(), users.countByRole(Role.STUDENT), users.countByRole(Role.ADMIN),
                subjects.count(), skills.count(), questions.count(), attempts.count());
    }
}
