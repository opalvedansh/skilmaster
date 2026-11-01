package com.skillmate.dto;

import com.skillmate.entity.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public final class TrackingDtos {
    private TrackingDtos() {}

    public record ProgressRequest(@NotNull(message = "Skill is required") Long skillId,
                                  @NotNull(message = "Status is required") ProgressStatus status) {}
    public record ProgressUpdateRequest(@NotNull(message = "Status is required") ProgressStatus status) {}
    public record ProgressDto(Long id, Long skillId, String skillName, Long subjectId, String subjectName,
                              ProgressStatus status, double percentage) {
        public static ProgressDto from(Progress p) {
            Skill s = p.getSkill();
            return new ProgressDto(p.getId(), s.getId(), s.getName(), s.getSubject().getId(), s.getSubject().getName(),
                    p.getStatus(), p.getPercentage().doubleValue());
        }
    }

    public record AttemptRequest(@NotNull(message = "Question is required") Long questionId,
                                 @NotNull(message = "Status is required") AttemptStatus status) {}
    public record AttemptDto(Long id, Long questionId, String questionText, Difficulty difficulty, String skillName,
                             String subjectName, AttemptStatus status, LocalDateTime attemptedAt) {
        public static AttemptDto from(QuestionAttempt a) {
            Question q = a.getQuestion();
            return new AttemptDto(a.getId(), q.getId(), q.getQuestionText(), q.getDifficulty(), q.getSkill().getName(),
                    q.getSkill().getSubject().getName(), a.getStatus(), a.getAttemptedAt());
        }
    }

    public static final String GITHUB_PATTERN = "^$|^https://(www\\.)?github\\.com/[A-Za-z0-9_.-]+(/[A-Za-z0-9_.-]+)*/?$";

    public record ProjectRequest(
            @NotBlank(message = "Title is required") @Size(max = 150, message = "Title must be at most 150 characters") String title,
            @Size(max = 5000, message = "Description is too long") String description,
            @Size(max = 255, message = "Technologies must be at most 255 characters") String technology,
            @Size(max = 255) @Pattern(regexp = GITHUB_PATTERN, message = "GitHub URL must look like https://github.com/user/repo") String githubUrl,
            @NotNull(message = "Status is required") ProjectStatus status) {}

    public record ProjectDto(Long id, String title, String description, String technology, String githubUrl, ProjectStatus status) {
        public static ProjectDto from(Project p) {
            return new ProjectDto(p.getId(), p.getTitle(), p.getDescription(), p.getTechnology(), p.getGithubUrl(), p.getStatus());
        }
    }
}
