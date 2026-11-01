package com.skillmate.dto;

import com.skillmate.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class CatalogDtos {
    private CatalogDtos() {}

    public record SubjectRequest(
            @NotBlank(message = "Name is required") @Size(max = 100, message = "Name must be at most 100 characters") String name,
            @Size(max = 5000, message = "Description is too long") String description) {}

    public record SubjectDto(Long id, String name, String description, long skillCount, double progressPercentage) {}

    public record SkillRequest(
            @NotBlank(message = "Name is required") @Size(max = 100, message = "Name must be at most 100 characters") String name,
            @NotNull(message = "Subject is required") Long subjectId) {}

    public record SkillDto(Long id, String name, Long subjectId, String subjectName, long questionCount,
                           Long progressId, ProgressStatus status, double percentage) {
        public boolean assessed() { return progressId != null && status != ProgressStatus.NOT_STARTED; }
    }

    public record QuestionRequest(
            @NotBlank(message = "Question text is required") @Size(max = 10000, message = "Question text is too long") String questionText,
            @NotNull(message = "Difficulty is required") Difficulty difficulty,
            @NotNull(message = "Skill is required") Long skillId) {}

    public record QuestionDto(Long id, String questionText, Difficulty difficulty, Long skillId, String skillName,
                              Long subjectId, String subjectName, AttemptStatus myStatus) {
        public static QuestionDto from(Question q, AttemptStatus myStatus) {
            Skill s = q.getSkill();
            return new QuestionDto(q.getId(), q.getQuestionText(), q.getDifficulty(), s.getId(), s.getName(),
                    s.getSubject().getId(), s.getSubject().getName(), myStatus);
        }
    }

    public static double toDouble(BigDecimal v) { return v == null ? 0 : v.doubleValue(); }
}
