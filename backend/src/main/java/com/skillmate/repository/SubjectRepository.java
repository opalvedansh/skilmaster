package com.skillmate.repository;

import com.skillmate.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findAllByOrderByNameAsc();
    Optional<Subject> findByNameIgnoreCase(String name);
}
