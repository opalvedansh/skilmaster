package com.skillmate.repository;

import com.skillmate.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.*;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findBySubjectIdOrderByNameAsc(Long subjectId);
    Optional<Skill> findBySubjectIdAndNameIgnoreCase(Long subjectId, String name);

    @Query("select s from Skill s order by s.subject.name, s.name")
    List<Skill> findAllOrdered();
}
