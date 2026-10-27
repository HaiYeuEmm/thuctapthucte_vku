ALTER TABLE assignment ADD COLUMN IF NOT EXISTS duration_minutes INT NULL;

ALTER TABLE exam_sessions DROP CONSTRAINT ck_exam_session_status;
ALTER TABLE exam_sessions
    ADD CONSTRAINT ck_exam_session_status
    CHECK (status IN ('ACTIVE', 'REVIEW_REQUIRED', 'LOCKED', 'SUBMITTED', 'TIME_EXPIRED'));
