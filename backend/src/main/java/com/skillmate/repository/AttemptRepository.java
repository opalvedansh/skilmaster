package com.skillmate.repository;

import com.skillmate.entity.AttemptStatus;
import com.skillmate.entity.QuestionAttempt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface AttemptRepository extends JpaRepository<QuestionAttempt, Long> {
    List<QuestionAttempt> findByUserIdOrderByAttemptedAtDescIdDesc(Long userId, Pageable page);
    List<QuestionAttempt> findByUserIdAndQuestionIdOrderByAttemptedAtDescIdDesc(Long userId, Long questionId);
    List<QuestionAttempt> findTop10ByUserIdOrderByAttemptedAtDescIdDesc(Long userId);
    Optional<QuestionAttempt> findFirstByUserIdAndQuestionIdOrderByAttemptedAtDescIdDesc(Long userId, Long questionId);
    long countByUserId(Long userId);

    @Query("select count(distinct a.question.id) from QuestionAttempt a where a.user.id = :uid")
    long countDistinctQuestions(@Param("uid") Long userId);

    @Query("select count(distinct a.question.id) from QuestionAttempt a where a.user.id = :uid and a.status = :status")
    long countDistinctQuestionsByStatus(@Param("uid") Long userId, @Param("status") AttemptStatus status);

    @Query("select count(distinct a.question.id) from QuestionAttempt a "
         + "where a.user.id = :uid and a.status = :status and a.question.skill.id = :skillId")
    long countDistinctQuestionsInSkill(@Param("uid") Long userId, @Param("status") AttemptStatus status, @Param("skillId") Long skillId);

    @Query("select a.question.id, a.status from QuestionAttempt a where a.user.id = :uid and a.question.id in :ids")
    List<Object[]> statusesFor(@Param("uid") Long userId, @Param("ids") Collection<Long> ids);
}
