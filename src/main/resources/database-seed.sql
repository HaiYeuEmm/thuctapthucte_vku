-- Sample data for database-schema.sql (MariaDB).
-- Run once, after creating the schema, on an empty database.
-- Demo passwords are plain text because the current login service compares
-- passwords directly. Replace these accounts/passwords for real use.

START TRANSACTION;

INSERT INTO app_users (id, full_name, email, password, role) VALUES
(1, 'Quản trị viên mẫu', 'admin@example.com', 'Admin123!', 'ADMIN'),
(2, 'Nguyễn Minh Anh', 'mentor.anh@example.com', 'Mentor123!', 'MENTOR'),
(3, 'Trần Quốc Bảo', 'mentor.bao@example.com', 'Mentor123!', 'MENTOR'),
(4, 'Lê Thu Hà', 'ha.student@example.com', 'Student123!', 'STUDENT'),
(5, 'Phạm Gia Huy', 'huy.student@example.com', 'Student123!', 'STUDENT'),
(6, 'Võ Ngọc Linh', 'linh.student@example.com', 'Student123!', 'STUDENT');

-- JOINED inheritance tables
INSERT INTO admin (id) VALUES (1);
INSERT INTO mentor (id) VALUES (2), (3);
INSERT INTO students (id) VALUES (4), (5), (6);

INSERT INTO training_class (id, name, term, department, major, mentor_id) VALUES
(1, 'Java Backend 01', 'HK1 2026', 'Công nghệ thông tin', 'Phát triển phần mềm', 2),
(2, 'Web Fullstack 01', 'HK1 2026', 'Công nghệ thông tin', 'Phát triển phần mềm', 3);

INSERT INTO training_class_students (class_id, student_id) VALUES
(1, 4), (1, 5), (2, 6);

INSERT INTO schedule (id, training_class_id, title, starts_at, ends_at, type) VALUES
(1, 1, 'Khai giảng và giới thiệu quy trình', '2026-10-20 09:00:00', '2026-10-20 11:00:00', 'STUDY'),
(2, 1, 'Review tiến độ tuần 1', '2026-10-23 14:00:00', '2026-10-23 15:00:00', 'WORK'),
(3, 2, 'Hạn nộp báo cáo tuần 1', '2026-10-25 08:00:00', '2026-10-25 17:00:00', 'SUBMISSION');

INSERT INTO assignment (id, training_class_id, title, description, deadline, type, language, max_score, version) VALUES
(1, 1, 'Báo cáo tuần đầu', 'Tóm tắt công việc, kết quả và khó khăn trong tuần đầu.', '2026-11-01 23:59:00', 'FILE', NULL, 10, 0),
(2, 1, 'Bài tập Java Collections', 'Viết chương trình xử lý danh sách nhân viên.', '2026-11-05 23:59:00', 'CODE', 'Java', 10, 0),
(3, 1, 'Quiz Java cơ bản', 'Kiểm tra kiến thức Java nền tảng.', '2026-11-08 23:59:00', 'QUIZ', NULL, 10, 0),
(4, 2, 'Giao diện trang giới thiệu', 'Tạo trang giới thiệu responsive bằng HTML và CSS.', '2026-11-06 23:59:00', 'CODE', 'HTML/CSS', 10, 0);

INSERT INTO quiz_question (id, assignment_id, question_text, option_a, option_b, option_c, option_d, correct_option, points) VALUES
(1, 3, 'Kiểu dữ liệu nào lưu giá trị đúng hoặc sai trong Java?', 'String', 'boolean', 'int', 'char', 'B', 5),
(2, 3, 'Từ khóa nào được dùng để tạo một lớp trong Java?', 'object', 'function', 'class', 'newclass', 'C', 5);

INSERT INTO assignment_submissions (id, assignment_id, student_id, file_name, stored_path, content_type, language, submitted_at, code, quiz_answers, score, feedback, version) VALUES
(1, 2, 4, NULL, NULL, NULL, 'Java', '2026-10-14 16:30:00', 'public class Main { public static void main(String[] args) { System.out.println("Hello internship"); } }', NULL, 8.5, 'Bài làm tốt, cần bổ sung xử lý trường hợp danh sách rỗng.', 0),
(2, 3, 4, NULL, NULL, NULL, NULL, '2026-10-14 10:15:00', NULL, '{"1":"B","2":"C"}', 10, 'Quiz tự chấm', 0);

INSERT INTO attendance (id, training_class_id, student_id, attendance_date, status) VALUES
(1, 1, 4, '2026-10-13', 'PRESENT'),
(2, 1, 5, '2026-10-13', 'LATE'),
(3, 1, 4, '2026-10-14', 'PRESENT'),
(4, 1, 5, '2026-10-14', 'EXCUSED'),
(5, 2, 6, '2026-10-13', 'PRESENT');

INSERT INTO evaluation (id, training_class_id, student_id, process_score, test_score, attendance_score, final_score, feedback, approved) VALUES
(1, 1, 4, 8.5, 9, 10, 9, 'Chủ động, hoàn thành công việc đúng hạn.', TRUE),
(2, 1, 5, 7.5, 8, 8, 7.8, 'Cần cải thiện việc cập nhật tiến độ.', FALSE),
(3, 2, 6, 9, 8.5, 9.5, 9, 'Làm việc tốt và phối hợp hiệu quả.', TRUE);

INSERT INTO learning_materials (id, title, description, file_name, file_type, file_url, file_size, created_at) VALUES
(1, 'Quy trình thực tập', 'Hướng dẫn các bước trong kỳ thực tập.', 'quy-trinh-thuc-tap.pdf', 'application/pdf', '/docs/quy-trinh-thuc-tap.pdf', 250000, '2026-10-15 09:00:00'),
(2, 'Giáo trình Java Core', 'Tài liệu Java Core dành cho sinh viên.', 'giao-trinh-java-core.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', '/docs/giao-trinh-java-core.docx', 520000, '2026-10-15 09:05:00'),
(3, 'Bài giảng Spring Boot', 'Các nội dung nền tảng về Spring Boot.', 'bai-giang-spring-boot.pptx', 'application/vnd.openxmlformats-officedocument.presentationml.presentation', '/docs/bai-giang-spring-boot.pptx', 780000, '2026-10-15 09:10:00');

INSERT INTO chat_groups (id, name, mentor_id, training_class_id) VALUES
(1, 'Java Backend 01 · Trao đổi chung', 2, 1),
(2, 'Web Fullstack 01 · Trao đổi chung', 3, 2);

INSERT INTO chat_group_members (group_id, user_id) VALUES
(1, 2), (1, 4), (1, 5),
(2, 3), (2, 6);

INSERT INTO chat_messages (id, group_id, sender_id, content, sent_at) VALUES
(1, 1, 2, 'Chào các bạn, nhóm mình dùng kênh này để trao đổi trong kỳ thực tập nhé.', '2026-10-15 09:15:00'),
(2, 1, 4, 'Em đã xem quy trình và sẽ gửi báo cáo tuần đầu đúng hạn ạ.', '2026-10-15 09:22:00'),
(3, 2, 3, 'Tuần này chúng ta tập trung hoàn thiện giao diện responsive.', '2026-10-15 10:00:00');

COMMIT;
