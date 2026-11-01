package com.skillmate.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "question_attempts")
public class QuestionAttempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(optional = false) @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private AttemptStatus status;
    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt;

    @PrePersist void onCreate() { if (attemptedAt == null) attemptedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public AttemptStatus getStatus() { return status; }
    public void setStatus(AttemptStatus status) { this.status = status; }
    public LocalDateTime getAttemptedAt() { return attemptedAt; }
}
