package com.skillmate.service;

import com.skillmate.dto.DashboardDtos.*;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Placement-readiness estimate (kept in one class so the formula is easy to change).
 *
 *  skill percentage      = progress percentage (0 when the skill has no progress record)
 *  subject readiness     = average skill percentage over ALL skills of the subject
 *  overall readiness     = average subject readiness over all subjects that have at least one skill
 *  Missing data counts as 0%, never as 100%. If no skill has been assessed at all, the result is NOT_ASSESSED.
 *
 *  A skill/subject is "assessed" when the student has started it (status != NOT_STARTED).
 *  WEAK < 40%, STRONG >= 70%, otherwise DEVELOPING. Unassessed items are never labelled WEAK.
 */
@Component
public class ReadinessCalculator {
    public static final double WEAK_THRESHOLD = 40;
    public static final double STRONG_THRESHOLD = 70;
    public static final String FORMULA = "Readiness = average of subject readiness; subject readiness = average progress of all its skills "
            + "(skills with no progress count as 0%). Weak < 40%, strong >= 70%.";
    public static final String DISCLAIMER = "This is an estimate based only on the progress you have recorded in SkillMate. "
            + "It is not a guarantee of placement or job eligibility.";

    public record SkillSnapshot(Long skillId, String skillName, Long subjectId, String subjectName, boolean assessed, double percentage) {}

    public static String classify(boolean assessed, double pct) {
        if (!assessed) return "NOT_ASSESSED";
        if (pct < WEAK_THRESHOLD) return "WEAK";
        return pct >= STRONG_THRESHOLD ? "STRONG" : "DEVELOPING";
    }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }

    public ReadinessDto calculate(List<SkillSnapshot> snaps) {
        Map<Long, List<SkillSnapshot>> bySubject = snaps.stream().collect(Collectors.groupingBy(SkillSnapshot::subjectId, LinkedHashMap::new, Collectors.toList()));
        List<SubjectReadiness> subjects = new ArrayList<>();
        for (List<SkillSnapshot> group : bySubject.values()) {
            double avg = round2(group.stream().mapToDouble(SkillSnapshot::percentage).average().orElse(0));
            int assessed = (int) group.stream().filter(SkillSnapshot::assessed).count();
            SkillSnapshot f = group.get(0);
            subjects.add(new SubjectReadiness(f.subjectId(), f.subjectName(), group.size(), assessed, avg, classify(assessed > 0, avg)));
        }
        subjects.sort(Comparator.comparing(SubjectReadiness::subjectName));

        int assessedSkills = (int) snaps.stream().filter(SkillSnapshot::assessed).count();
        boolean assessed = assessedSkills > 0;
        double overall = assessed ? round2(subjects.stream().mapToDouble(SubjectReadiness::percentage).average().orElse(0)) : 0;
        String level = !assessed ? "NOT_ASSESSED" : overall >= STRONG_THRESHOLD ? "STRONG" : overall >= WEAK_THRESHOLD ? "DEVELOPING" : "EARLY_STAGE";

        List<SkillInsight> strong = snaps.stream().filter(s -> s.assessed() && s.percentage() >= STRONG_THRESHOLD)
                .sorted(Comparator.comparingDouble(SkillSnapshot::percentage).reversed()).map(ReadinessCalculator::insight).toList();
        List<SkillInsight> weak = snaps.stream().filter(s -> s.assessed() && s.percentage() < WEAK_THRESHOLD)
                .sorted(Comparator.comparingDouble(SkillSnapshot::percentage)).map(ReadinessCalculator::insight).toList();

        List<Recommendation> recs = subjects.stream().filter(s -> !s.classification().equals("STRONG"))
                .sorted(Comparator.comparingInt((SubjectReadiness s) -> priority(s.classification())).thenComparingDouble(SubjectReadiness::percentage))
                .limit(4).map(ReadinessCalculator::recommend).toList();

        return new ReadinessDto(overall, level, assessed, assessedSkills, snaps.size(), subjects, strong, weak, recs,
                WEAK_THRESHOLD, STRONG_THRESHOLD, FORMULA, DISCLAIMER);
    }

    private static int priority(String c) { return switch (c) { case "WEAK" -> 0; case "NOT_ASSESSED" -> 1; default -> 2; }; }

    private static SkillInsight insight(SkillSnapshot s) { return new SkillInsight(s.skillId(), s.skillName(), s.subjectName(), s.percentage()); }

    private static Recommendation recommend(SubjectReadiness s) {
        String reason = switch (s.classification()) {
            case "WEAK" -> "Progress is " + s.percentage() + "%, below the " + (int) WEAK_THRESHOLD + "% threshold. Practise more questions here.";
            case "NOT_ASSESSED" -> "No progress recorded yet. Start one skill to get an assessment.";
            default -> "Progress is " + s.percentage() + "%. Keep practising to reach " + (int) STRONG_THRESHOLD + "%.";
        };
        return new Recommendation(s.subjectId(), s.subjectName(), s.classification(), reason);
    }
}
