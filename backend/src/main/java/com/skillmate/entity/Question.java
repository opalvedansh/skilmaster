package com.skillmate.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "question_text", nullable = false)
    private String questionText;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Difficulty difficulty;
    @ManyToOne(optional = false) @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    public Long getId() { return id; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }
}
