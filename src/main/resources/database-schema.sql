-- Canonical schema for MariaDB (database: tttt).
-- Apply to a new/empty database. This file intentionally does not drop or
-- rewrite existing data; back up and migrate an existing installation first.

CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_app_users_email UNIQUE (email),
    CONSTRAINT ck_app_users_role CHECK (role IN ('ADMIN', 'MENTOR', 'STUDENT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- JOINED inheritance tables used by User's current JPA mapping.
CREATE TABLE IF NOT EXISTS admin (
    id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_admin_user FOREIGN KEY (id) REFERENCES app_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS mentor (
    id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_mentor_user FOREIGN KEY (id) REFERENCES app_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS students (
    id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_students_user FOREIGN KEY (id) REFERENCES app_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS training_class (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    term VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    major VARCHAR(255) NOT NULL,
    mentor_id BIGINT NULL,
    PRIMARY KEY (id),
    KEY idx_training_class_mentor (mentor_id),
    CONSTRAINT fk_training_class_mentor FOREIGN KEY (mentor_id) REFERENCES app_users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS training_class_students (
    class_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    PRIMARY KEY (class_id, student_id),
    KEY idx_class_students_student (student_id),
    CONSTRAINT fk_class_students_class FOREIGN KEY (class_id) REFERENCES training_class(id) ON DELETE CASCADE,
    CONSTRAINT fk_class_students_user FOREIGN KEY (student_id) REFERENCES app_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS schedule (
    id BIGINT NOT NULL AUTO_INCREMENT,
    training_class_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    starts_at DATETIME(6) NOT NULL,
    ends_at DATETIME(6) NOT NULL,
    type VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_schedule_class_start (training_class_id, starts_at),
    CONSTRAINT fk_schedule_class FOREIGN KEY (training_class_id) REFERENCES training_class(id) ON DELETE CASCADE,
    CONSTRAINT ck_schedule_type CHECK (type IN ('STUDY', 'WORK', 'SUBMISSION', 'HOLIDAY')),
    CONSTRAINT ck_schedule_time CHECK (ends_at > starts_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS assignment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    training_class_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NULL,
    deadline DATETIME(6) NOT NULL,
    type VARCHAR(20) NOT NULL,
    language VARCHAR(80) NULL,
    max_score DOUBLE NOT NULL DEFAULT 10,
    duration_minutes INT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_assignment_class_deadline (training_class_id, deadline),
    CONSTRAINT fk_assignment_class FOREIGN KEY (training_class_id) REFERENCES training_class(id) ON DELETE CASCADE,
    CONSTRAINT ck_assignment_type CHECK (type IN ('FILE', 'CODE', 'QUIZ')),
    CONSTRAINT ck_assignment_score CHECK (max_score > 0 AND max_score <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE assignment ADD COLUMN IF NOT EXISTS duration_minutes INT NULL;

CREATE TABLE IF NOT EXISTS quiz_question (
    id BIGINT NOT NULL AUTO_INCREMENT,
    assignment_id BIGINT NOT NULL,
    question_text VARCHAR(2000) NOT NULL,
    option_a VARCHAR(1000) NOT NULL,
    option_b VARCHAR(1000) NOT NULL,
    option_c VARCHAR(1000) NOT NULL,
    option_d VARCHAR(1000) NOT NULL,
    correct_option VARCHAR(1) NOT NULL,
    points DOUBLE NOT NULL,
    PRIMARY KEY (id),
    KEY idx_quiz_question_assignment (assignment_id),
    CONSTRAINT fk_quiz_assignment FOREIGN KEY (assignment_id) REFERENCES assignment(id) ON DELETE CASCADE,
    CONSTRAINT ck_quiz_correct_option CHECK (correct_option IN ('A', 'B', 'C', 'D')),
    CONSTRAINT ck_quiz_points CHECK (points > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS exam_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    assignment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(24) NOT NULL,
    violation_count INT NOT NULL DEFAULT 0,
    last_violation VARCHAR(200) NULL,
    started_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_exam_session_assignment_student UNIQUE (assignment_id, student_id),
    KEY idx_exam_session_student_status (student_id, status),
    KEY idx_exam_session_class_lookup (assignment_id, status),
    CONSTRAINT fk_exam_session_assignment FOREIGN KEY (assignment_id) REFERENCES assignment(id) ON DELETE CASCADE,
    CONSTRAINT fk_exam_session_student FOREIGN KEY (student_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT ck_exam_session_status CHECK (status IN ('ACTIVE', 'REVIEW_REQUIRED', 'LOCKED', 'SUBMITTED', 'TIME_EXPIRED')),
    CONSTRAINT ck_exam_session_violations CHECK (violation_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS assignment_submissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    assignment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    file_name VARCHAR(255) NULL,
    stored_path VARCHAR(500) NULL,
    content_type VARCHAR(150) NULL,
    language VARCHAR(80) NULL,
    submitted_at DATETIME(6) NOT NULL,
    code LONGTEXT NULL,
    quiz_answers LONGTEXT NULL,
    score DOUBLE NULL,
    feedback VARCHAR(2000) NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uq_submission_assignment_student UNIQUE (assignment_id, student_id),
    KEY idx_submission_student (student_id),
    KEY idx_submission_assignment_time (assignment_id, submitted_at),
    CONSTRAINT fk_submission_assignment FOREIGN KEY (assignment_id) REFERENCES assignment(id) ON DELETE CASCADE,
    CONSTRAINT fk_submission_student FOREIGN KEY (student_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT ck_submission_score CHECK (score IS NULL OR score >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT NOT NULL AUTO_INCREMENT,
    training_class_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_attendance_class_student_date UNIQUE (training_class_id, student_id, attendance_date),
    KEY idx_attendance_student_date (student_id, attendance_date),
    CONSTRAINT fk_attendance_class FOREIGN KEY (training_class_id) REFERENCES training_class(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT ck_attendance_status CHECK (status IN ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS evaluation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    training_class_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    process_score DOUBLE NULL,
    test_score DOUBLE NULL,
    attendance_score DOUBLE NULL,
    final_score DOUBLE NULL,
    feedback VARCHAR(2000) NULL,
    approved BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT uq_evaluation_class_student UNIQUE (training_class_id, student_id),
    KEY idx_evaluation_student (student_id),
    CONSTRAINT fk_evaluation_class FOREIGN KEY (training_class_id) REFERENCES training_class(id) ON DELETE CASCADE,
    CONSTRAINT fk_evaluation_student FOREIGN KEY (student_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT ck_evaluation_process_score CHECK (process_score IS NULL OR process_score BETWEEN 0 AND 10),
    CONSTRAINT ck_evaluation_test_score CHECK (test_score IS NULL OR test_score BETWEEN 0 AND 10),
    CONSTRAINT ck_evaluation_attendance_score CHECK (attendance_score IS NULL OR attendance_score BETWEEN 0 AND 10),
    CONSTRAINT ck_evaluation_final_score CHECK (final_score IS NULL OR final_score BETWEEN 0 AND 10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS learning_materials (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255) NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(255) NULL,
    file_url VARCHAR(255) NOT NULL,
    file_size BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_learning_material_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chat_groups (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    mentor_id BIGINT NOT NULL,
    training_class_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_chat_group_class (training_class_id),
    KEY idx_chat_group_mentor (mentor_id),
    CONSTRAINT fk_chat_group_mentor FOREIGN KEY (mentor_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_group_class FOREIGN KEY (training_class_id) REFERENCES training_class(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chat_group_members (
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (group_id, user_id),
    KEY idx_chat_members_user (user_id),
    CONSTRAINT fk_chat_members_group FOREIGN KEY (group_id) REFERENCES chat_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_members_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content VARCHAR(2000) NOT NULL,
    sent_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_chat_message_group_time (group_id, sent_at),
    KEY idx_chat_message_sender (sender_id),
    CONSTRAINT fk_chat_message_group FOREIGN KEY (group_id) REFERENCES chat_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_message_sender FOREIGN KEY (sender_id) REFERENCES app_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
