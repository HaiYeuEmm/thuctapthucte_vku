<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Không gian Mentor</title>
    <link rel="stylesheet" href="/css/app.css">
    <link rel="stylesheet" href="/css/portal.css">
</head>
<body>
    <div class="shell">
        <aside class="sidebar">
            <div class="brand">
                INTERNSHIP OS
                <small>Không gian mentor</small>
            </div>
            <nav class="nav">
                <button class="active" data-view="mentor-home" data-title="Lớp phụ trách">
                    <span>01</span>Lớp phụ trách
                </button>
                <div class="nav-group" id="mentor-training-nav">
                    <button id="mentor-training-toggle" class="nav-parent" type="button" aria-expanded="false" aria-controls="mentor-training-submenu">
                        <span>02</span>Đào tạo<i aria-hidden="true">⌄</i>
                    </button>
                    <div class="nav-submenu" id="mentor-training-submenu">
                        <button data-view="mentor-schedule" data-title="Tạo lịch trình">Tạo lịch trình</button>
                        <button data-view="mentor-assignment" data-title="Tạo bài thi">Tạo bài thi</button>
                        <button data-view="mentor-materials" data-title="Giáo trình">Giáo trình</button>
                    </div>
                </div>
                <button data-view="mentor-attendance" data-title="Điểm danh">
                    <span>03</span>Điểm danh
                </button>
                <button data-view="mentor-evaluation" data-title="Đánh giá cuối kỳ">
                    <span>04</span>Đánh giá
                </button>
                <button data-view="mentor-exams" data-title="Giám sát bài kiểm tra">
                    <span>05</span>Giám sát kiểm tra
                </button>
                <button data-view="mentor-message" data-title="Trao đổi">
                    <span>06</span>Trao đổi
                </button>
                <a href="/logout" style="margin-top:28px;padding:12px;color:#f0a5a5;text-decoration:none;font-size:13px">Đăng xuất</a>
            </nav>
        </aside>

        <main class="main">
            <input id="mentor-id" type="hidden" value="${mentorId}">
            <header class="topbar">
                <div>
                    <div class="eyebrow">Mentor · Kỳ thực tập 2026</div>
                    <h1 id="page-title">Lớp phụ trách</h1>
                    <p class="subtle">Theo dõi tiến độ, giao việc và phản hồi cho sinh viên.</p>
                </div>
                <div class="avatar">
                    <span class="avatar-mark">M</span>Giảng viên hướng dẫn
                </div>
            </header>

            <!-- SECTION 1: LỚP PHỤ TRÁCH -->
            <section id="mentor-home" class="view active">
                <div class="portal-hero">
                    <div>
                        <div class="hero-kicker">Bảng điều phối</div>
                        <h2>Biến từng tuần thực tập thành một bước tiến rõ ràng.</h2>
                        <p>Quản lý lớp, đặt nhịp đào tạo và phản hồi dựa trên dữ liệu điểm danh, bài tập và đánh giá.</p>
                    </div>
                    <div class="hero-progress">
                        <span class="hero-progress-mark">01</span>
                        <div><strong>Không gian mentor</strong><small>Lịch trình · bài tập · đánh giá</small></div>
                    </div>
                </div>

                <div class="role-note">Chọn lớp trong tab Đào tạo để xem lịch, bài tập và kết quả đánh giá của nhóm sinh viên.</div>

                <div class="panel" style="margin-top:18px">
                    <div class="panel-head">
                        <h2>Lớp được phân công</h2>
                        <span class="badge">Mentor</span>
                    </div>
                    <ul id="class-list" class="list">
                        <li class="subtle">Đang tải dữ liệu...</li>
                    </ul>
                </div>
            </section>

            <!-- SECTION 2: TẠO LỊCH TRÌNH -->
            <section id="mentor-schedule" class="view">
                <div class="panel" style="margin-bottom:18px">
                    <div class="panel-head">
                        <h2>Lịch trình đã tạo</h2>
                    </div>
                    <ul id="schedule-list" class="list">
                        <li class="subtle">Chọn lớp để tải dữ liệu.</li>
                    </ul>
                </div>

                <div class="panel" style="margin-bottom:18px">
                    <div class="panel-head">
                        <h2>Tạo lịch trình</h2>
                        <span class="subtle">Thời gian kết thúc phải sau thời gian bắt đầu</span>
                    </div>
                    <form id="schedule-form">
                        <div class="form-grid">
                            <div>
                                <label>Lớp</label>
                                <select id="mentor-schedule-class" name="classId" data-classes data-mentor-class required></select>
                            </div>
                            <div>
                                <label>Loại lịch</label>
                                <select name="type" required>
                                    <option value="STUDY">Học tập</option>
                                    <option value="WORK">Làm việc</option>
                                    <option value="SUBMISSION">Hạn nộp</option>
                                    <option value="HOLIDAY">Ngày nghỉ</option>
                                </select>
                            </div>
                            <div>
                                <label>Tiêu đề</label>
                                <input name="title" placeholder="Ví dụ: Review tiến độ tuần 2" required>
                            </div>
                            <div>
                                <label>Bắt đầu</label>
                                <input name="startsAt" type="datetime-local" required>
                            </div>
                            <div>
                                <label>Kết thúc</label>
                                <input name="endsAt" type="datetime-local" required>
                            </div>
                        </div>
                        <div class="form-actions">
                            <button class="btn" type="submit">Lưu lịch trình</button>
                        </div>
                    </form>
                </div>

            </section>

            <!-- SECTION 3: THÊM BÀI TẬP -->
            <section id="mentor-assignment" class="view">
                <div class="panel" style="margin-bottom:18px">
                    <div class="panel-head">
                        <h2>Bài tập đã giao</h2>
                    </div>
                    <ul id="assignment-list" class="list">
                        <li class="subtle">Chọn lớp để tải dữ liệu.</li>
                    </ul>
                </div>

                <div class="panel">
                    <div class="panel-head">
                        <h2>Tạo bài thi</h2>
                        <span class="subtle">Thời hạn mở bài phải ở hiện tại hoặc tương lai</span>
                    </div>
                    <form id="mentor-assignment-form">
                        <div class="form-grid">
                            <div>
                                <label>Lớp</label>
                                <select id="mentor-assignment-class" name="classId" data-classes data-mentor-class required></select>
                            </div>
                            <div>
                                <label>Thời hạn mở bài</label>
                                <input name="deadline" type="datetime-local" required>
                            </div>
                            <div>
                                <label>Tên bài tập</label>
                                <input name="title" required placeholder="Ví dụ: Báo cáo tuần 1">
                            </div>
                            <div id="assignment-description-field" hidden>
                                <label id="assignment-description-label">Mô tả</label>
                                <textarea name="description" rows="3" placeholder="Yêu cầu và tiêu chí đánh giá"></textarea>
                            </div>
                            <div>
                                <label>Dạng bài tập</label>
                                <select name="type" id="assignment-type" required>
                                    <option value="">Chọn dạng bài tập</option>
                                    <option value="FILE">Nộp file</option>
                                    <option value="CODE">Bài kiểm tra code</option>
                                    <option value="QUIZ">Quiz trắc nghiệm</option>
                                </select>
                            </div>
                            <div id="assignment-duration-field" hidden>
                                <label>Thời gian làm bài (phút)</label>
                                <input name="durationMinutes" type="number" min="1" max="300" step="1" placeholder="Ví dụ: 30">
                                <small class="subtle">Đồng hồ bắt đầu khi học sinh mở bài thi.</small>
                            </div>
                            <div id="assignment-language-field" hidden>
                                <label>Ngôn ngữ lập trình</label>
                                <input name="language" placeholder="Ví dụ: Java, Python, JavaScript">
                            </div>
                        </div>
                        <section id="quiz-question-editor" class="quiz-editor" aria-label="Danh sách câu hỏi quiz" hidden>
                            <div class="quiz-editor-head">
                                <div>
                                    <h3>Câu hỏi trắc nghiệm</h3>
                                    <p class="subtle">Thêm một hoặc nhiều câu hỏi. Mỗi câu có 4 lựa chọn và một đáp án đúng.</p>
                                </div>
                                <button class="btn secondary" id="add-quiz-question" type="button">＋ Thêm câu hỏi</button>
                            </div>
                            <div id="quiz-question-list" class="quiz-question-list"></div>
                        </section>
                        <template id="quiz-question-template">
                            <fieldset class="quiz-authoring-card">
                                <legend class="quiz-authoring-title">Câu hỏi</legend>
                                <button class="quiz-question-remove" type="button" aria-label="Xóa câu hỏi">Xóa</button>
                                <div class="form-grid">
                                    <div class="quiz-question-prompt">
                                        <label>Nội dung câu hỏi</label>
                                        <textarea name="questionText" rows="2" required></textarea>
                                    </div>
                                    <div>
                                        <label>Đáp án đúng</label>
                                        <select name="correctOption" required>
                                            <option value="A">A</option>
                                            <option value="B">B</option>
                                            <option value="C">C</option>
                                            <option value="D">D</option>
                                        </select>
                                    </div>
                                    <div>
                                        <label>Điểm câu hỏi</label>
                                        <input name="points" type="number" min="0.1" step="0.1" value="1" required>
                                    </div>
                                    <div><label>Phương án A</label><input name="optionA" required></div>
                                    <div><label>Phương án B</label><input name="optionB" required></div>
                                    <div><label>Phương án C</label><input name="optionC" required></div>
                                    <div><label>Phương án D</label><input name="optionD" required></div>
                                </div>
                            </fieldset>
                        </template>
                        <div class="form-actions">
                            <button class="btn" type="submit">Tạo bài thi</button>
                        </div>
                    </form>
                </div>
            </section>

            <section id="mentor-materials" class="view">
                <div class="panel">
                    <div class="panel-head">
                        <div><div class="eyebrow">Tài liệu học tập</div><h2>Giáo trình</h2></div>
                    </div>
                    <p class="subtle">Tải lên giáo trình và tài liệu để sinh viên trong các lớp có thể xem và tải xuống.</p>
                    <form id="mentor-material-form" class="material-upload-form" enctype="multipart/form-data">
                        <div class="form-grid">
                            <div>
                                <label for="mentor-material-title">Tên giáo trình</label>
                                <input id="mentor-material-title" name="title" maxlength="200" placeholder="Ví dụ: Giáo trình Java cơ bản" required>
                            </div>
                            <div>
                                <label for="mentor-material-file">Tệp giáo trình</label>
                                <input id="mentor-material-file" name="file" type="file" required>
                                <small class="subtle">Dung lượng tối đa 10 MB.</small>
                            </div>
                            <div class="material-description-field">
                                <label for="mentor-material-description">Mô tả</label>
                                <textarea id="mentor-material-description" name="description" rows="3" maxlength="1000" placeholder="Thông tin về nội dung giáo trình (không bắt buộc)"></textarea>
                            </div>
                        </div>
                        <div class="form-actions">
                            <button class="btn" type="submit">Tải giáo trình lên</button>
                        </div>
                    </form>
                </div>
                <div class="panel" style="margin-top:18px">
                    <div class="panel-head"><h2>Giáo trình đã tải lên</h2></div>
                    <div id="mentor-material-list" class="material-list"><p class="subtle">Đang tải giáo trình...</p></div>
                </div>
            </section>

            <!-- SECTION 4: ĐIỂM DANH -->
            <section id="mentor-attendance" class="view">
                <div class="panel">
                    <div class="panel-head">
                        <div><div class="eyebrow">Theo dõi chuyên cần</div><h2>Điểm danh học sinh</h2></div>
                    </div>
                    <form id="attendance-form">
                        <div class="attendance-toolbar">
                            <div>
                                <label for="attendance-class">Lớp</label>
                                <select id="attendance-class" name="classId" data-classes required></select>
                            </div>
                            <div>
                                <label for="attendance-date">Ngày điểm danh</label>
                                <input id="attendance-date" name="date" type="date" required>
                                <small class="attendance-date-hint">Mặc định là ngày hiện tại; chỉ cần chọn một ngày nếu điểm danh bù.</small>
                            </div>
                        </div>
                        <div class="table-wrap attendance-roster-wrap">
                            <table class="attendance-roster">
                                <thead><tr><th>Học sinh</th><th>Đi trễ</th><th>Có mặt</th><th>Có phép</th><th>Vắng</th></tr></thead>
                                <tbody id="attendance-roster"><tr><td colspan="5" class="subtle">Chọn lớp để tải danh sách học sinh.</td></tr></tbody>
                            </table>
                        </div>
                        <div class="form-actions attendance-actions">
                            <button class="btn" id="attendance-save" type="submit">Lưu điểm danh</button>
                        </div>
                    </form>
                </div>
            </section>

            <!-- SECTION 5: ĐÁNH GIÁ -->
            <section id="mentor-evaluation" class="view">
                <div class="panel">
                    <div class="panel-head">
                        <div>
                            <div class="eyebrow">Theo dõi kết quả học tập</div>
                            <h2>Đánh giá sinh viên</h2>
                        </div>
                        <select id="evaluation-class-filter" aria-label="Chọn lớp để đánh giá"></select>
                    </div>
                    <div id="evaluation-student-list" class="evaluation-student-list">
                        <p class="subtle">Đang tải danh sách sinh viên...</p>
                    </div>
                </div>
                <div id="evaluation-student-detail" class="panel evaluation-detail" hidden></div>
            </section>

            <!-- SECTION 6: EXAM MONITOR -->
            <section id="mentor-exams" class="view">
                <div class="panel">
                    <div class="panel-head">
                        <div><div class="eyebrow">An toàn bài kiểm tra</div><h2>Phiên cần mentor xem xét</h2></div>
                        <select id="exam-class-filter" data-mentor-classes aria-label="Chọn lớp để giám sát"></select>
                    </div>
                    <p class="subtle">Sinh viên bị tạm khóa khi rời tab hoặc thoát chế độ toàn màn hình. Chọn cho phép tiếp tục hoặc khóa bài thi.</p>
                    <div class="table-wrap">
                        <table>
                            <thead><tr><th>Sinh viên</th><th>Bài kiểm tra</th><th>Trạng thái</th><th>Sự kiện</th><th>Thời điểm</th><th>Quyết định</th></tr></thead>
                            <tbody id="exam-session-table"><tr><td colspan="6" class="subtle">Chọn lớp để xem phiên cần duyệt.</td></tr></tbody>
                        </table>
                    </div>
                </div>
            </section>

            <!-- SECTION 7: TRAO ĐỔI -->
            <section id="mentor-message" class="view">
                <div class="panel">
                    <div class="panel-head">
                        <h2>Trao đổi với sinh viên</h2>
                        <select id="mentor-chat-class" aria-label="Chọn lớp trao đổi"></select>
                    </div>
                    <p id="mentor-chat-group-label" class="subtle">Chọn lớp để xem tin nhắn.</p>
                    <div id="mentor-chat-messages" class="message-box" aria-live="polite">
                        <div class="subtle">Chọn lớp để tải cuộc trao đổi.</div>
                    </div>
                    <form id="mentor-chat-form" class="form-actions mentor-chat-form">
                        <input id="mentor-chat-input" placeholder="Viết tin nhắn cho lớp..." aria-label="Nội dung tin nhắn" maxlength="2000" required>
                        <button class="btn" type="submit">Gửi tin nhắn</button>
                    </form>
                </div>
            </section>
        </main>
    </div>

    <div id="toast" class="toast"></div>

    <!-- SCRIPTS -->
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
    <script src="/js/portal.js?v=mentor-materials-20261017"></script>
    <script src="/js/realtime.js"></script>
</body>
</html>
