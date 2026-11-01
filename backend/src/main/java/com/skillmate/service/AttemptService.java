package com.skillmate.service;

import com.skillmate.dto.TrackingDtos.*;
import com.skillmate.entity.*;
import com.skillmate.exception.ApiException;
import com.skillmate.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AttemptService {
    /** An identical attempt (same user, question, status) inside this window is treated as a retry, not a new attempt. */
    public static final int DEDUP_SECONDS = 10;
    public static final int MAX_LIST = 500;
    private final AttemptRepository attempts; private final QuestionRepository questions; private final UserRepository users;
    private final ProgressService progressService;

    public AttemptService(AttemptRepository attempts, QuestionRepository questions, UserRepository users, ProgressService progressService) {
        this.attempts = attempts; this.questions = questions; this.users = users; this.progressService = progressService;
    }

    @Transactional(readOnly = true)
    public List<AttemptDto> listMine(Long userId, int limit) {
        return attempts.findByUserIdOrderByAttemptedAtDescIdDesc(userId, PageRequest.of(0, Math.min(Math.max(limit, 1), MAX_LIST))).stream().map(AttemptDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AttemptDto> forQuestion(Long userId, Long questionId) {
        return attempts.findByUserIdAndQuestionIdOrderByAttemptedAtDescIdDesc(userId, questionId).stream().map(AttemptDto::from).toList();
    }

    public AttemptDto record(Long userId, AttemptRequest r) {
        Question q = questions.findById(r.questionId()).orElseThrow(() -> ApiException.notFound("Question"));
        // Serialises this user's attempts: a double-click can't create two progress rows (409) or slip past the dedup check
        User user = users.lockById(userId).orElseThrow(() -> ApiException.notFound("User"));
        var last = attempts.findFirstByUserIdAndQuestionIdOrderByAttemptedAtDescIdDesc(userId, q.getId());
        if (last.isPresent() && last.get().getStatus() == r.status()
                && last.get().getAttemptedAt().isAfter(LocalDateTime.now().minusSeconds(DEDUP_SECONDS))) {
            return AttemptDto.from(last.get());
        }
        QuestionAttempt a = new QuestionAttempt();
        a.setUser(user); a.setQuestion(q); a.setStatus(r.status());
        a = attempts.save(a);
        progressService.onAttempt(userId, q);
        return AttemptDto.from(a);
    }
}
