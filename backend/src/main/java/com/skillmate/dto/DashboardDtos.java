package com.skillmate.dto;

import java.util.List;

public final class DashboardDtos {
    private DashboardDtos() {}

    public record SkillInsight(Long skillId, String skillName, String subjectName, double percentage) {}

    public record DashboardSummary(String name, long totalSubjects, long totalSkills, long completedSkills, long inProgressSkills,
                                   long questionsAttempted, long questionsSolved, double overallProgress,
                                   double readinessPercentage, String readinessLevel, boolean readinessAssessed,
                                   List<TrackingDtos.AttemptDto> recentAttempts,
                                   List<SkillInsight> strongestSkills, List<SkillInsight> skillsToImprove) {}

    public record SubjectProgress(Long subjectId, String subjectName, int skillCount, int completedSkills, int inProgressSkills, double percentage) {}

    public record AttemptStats(long totalAttempts, long questionsAttempted, long questionsSolved, long totalQuestions, double solveRate) {}

    public record ProgressOverview(double overallProgress, int completedSkills, int inProgressSkills, int pendingSkills,
                                   List<SubjectProgress> subjects, List<CatalogDtos.SkillDto> skills, AttemptStats attemptStats) {}

    public record SubjectReadiness(Long subjectId, String subjectName, int skillCount, int assessedSkills, double percentage, String classification) {}

    public record Recommendation(Long subjectId, String subjectName, String type, String reason) {}

    public record ReadinessDto(double overallPercentage, String level, boolean assessed, int assessedSkills, int totalSkills,
                               List<SubjectReadiness> subjects, List<SkillInsight> strongSkills, List<SkillInsight> weakSkills,
                               List<Recommendation> recommendations, double weakThreshold, double strongThreshold,
                               String formula, String disclaimer) {}

    public record AdminStats(long totalUsers, long totalStudents, long totalAdmins, long totalSubjects, long totalSkills,
                             long totalQuestions, long totalAttempts) {}
}
