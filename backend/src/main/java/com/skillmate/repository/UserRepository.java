package com.skillmate.repository;

import com.skillmate.entity.Role;
import com.skillmate.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    long countByRole(Role role);
    /** Row lock that serialises one user's progress writes (see AttemptService.record). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> lockById(@Param("id") Long id);

    @Query("select u from User u where (:role is null or u.role = :role) and "
         + "(:q = '' or locate(lower(:q), lower(u.name)) > 0 or locate(lower(:q), lower(u.email)) > 0)")
    Page<User> search(@Param("q") String q, @Param("role") Role role, Pageable pageable);
}
