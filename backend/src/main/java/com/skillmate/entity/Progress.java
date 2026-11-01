package com.skillmate.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "progress", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_id"}))
public class Progress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(optional = false) @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private ProgressStatus status = ProgressStatus.NOT_STARTED;
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage = BigDecimal.ZERO;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }
    public ProgressStatus getStatus() { return status; }
    public void setStatus(ProgressStatus status) { this.status = status; }
    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
}
