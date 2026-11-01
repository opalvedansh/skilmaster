package com.skillmate.repository;

import com.skillmate.entity.Difficulty;
import com.skillmate.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("select q from Question q where (:skillId is null or q.skill.id = :skillId) "
         + "and (:subjectId is null or q.skill.subject.id = :subjectId) "
         + "and (:difficulty is null or q.difficulty = :difficulty) "
         + "and (:search = '' or locate(lower(:search), lower(q.questionText)) > 0)")
    Page<Question> search(@Param("skillId") Long skillId, @Param("subjectId") Long subjectId,
                          @Param("difficulty") Difficulty difficulty, @Param("search") String search, Pageable pageable);

    long countBySkillId(Long skillId);

    @Query("select q.skill.id, count(q) from Question q group by q.skill.id")
    List<Object[]> countGroupedBySkill();
}
