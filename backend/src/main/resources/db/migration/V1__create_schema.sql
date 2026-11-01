-- SkillMate schema (MySQL 8). Compatible with H2 in MySQL mode for tests.
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_users_email UNIQUE (email),
  CONSTRAINT ck_users_role CHECK (role IN ('STUDENT', 'ADMIN'))
);

CREATE TABLE subjects (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description TEXT,
  CONSTRAINT uq_subjects_name UNIQUE (name)
);

CREATE TABLE skills (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  subject_id BIGINT NOT NULL,
  CONSTRAINT fk_skills_subject FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE CASCADE,
  CONSTRAINT uq_skills_subject_name UNIQUE (subject_id, name)
);
CREATE INDEX idx_skills_subject ON skills (subject_id);

CREATE TABLE questions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  question_text TEXT NOT NULL,
  difficulty VARCHAR(20) NOT NULL,
  skill_id BIGINT NOT NULL,
  CONSTRAINT fk_questions_skill FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE CASCADE,
  CONSTRAINT ck_questions_difficulty CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD'))
);
CREATE INDEX idx_questions_skill_difficulty ON questions (skill_id, difficulty);

CREATE TABLE progress (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  skill_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  percentage DECIMAL(5,2) NOT NULL DEFAULT 0,
  CONSTRAINT fk_progress_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_progress_skill FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE CASCADE,
  CONSTRAINT uq_progress_user_skill UNIQUE (user_id, skill_id),
  CONSTRAINT ck_progress_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')),
  CONSTRAINT ck_progress_pct CHECK (percentage >= 0 AND percentage <= 100)
);

CREATE TABLE question_attempts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,
  attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_attempts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_attempts_question FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE,
  CONSTRAINT ck_attempts_status CHECK (status IN ('ATTEMPTED', 'SOLVED'))
);
CREATE INDEX idx_attempts_user_question ON question_attempts (user_id, question_id);
CREATE INDEX idx_attempts_user_time ON question_attempts (user_id, attempted_at);

CREATE TABLE projects (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  title VARCHAR(150) NOT NULL,
  description TEXT,
  technology VARCHAR(255),
  github_url VARCHAR(255),
  status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
  CONSTRAINT fk_projects_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT ck_projects_status CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED'))
);
CREATE INDEX idx_projects_user ON projects (user_id);
