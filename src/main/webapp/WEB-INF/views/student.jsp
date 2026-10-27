<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Không gian Sinh viên</title>
    <link rel="stylesheet" href="/css/app.css">
    <link rel="stylesheet" href="/css/portal.css">
</head>
<body>
    <div class="shell">
        <aside class="sidebar">
            <div class="brand">
                INTERNSHIP
                <small>Sinh viên HN</small>
            </div>
            <nav class="nav">
                <button class="active" data-view="student-home" data-title="Hành trình của tôi">
                    <span>01</span>Lịch trình thực tập 
                </button>
                <div class="nav-group" id="student-work-nav">
                    <button id="student-work-toggle" class="nav-parent" type="button" aria-expanded="false" aria-controls="student-work-submenu">
                        <span>02</span>Bài tập<i aria-hidden="true">⌄</i>
                    </button>
                    <div class="nav-submenu" id="student-work-submenu">
                        <button data-view="student-submit" data-title="Nộp bài tập">Nộp bài tập</button>
                        <button data-view="student-test" data-title="Làm bài kiểm tra">Làm bài kiểm tra</button>
                    </div>
                </div>
                <button data-view="student-materials" data-title="Tài liệu">
                    <span>03</span>Giáo trình
                </button>
                <button data-view="student-message" data-title="Trao đổi">
                    <span>04</span>Trao đổi
                </button>
                <a href="/logout" style="margin-top:28px;padding:12px;color:#f0a5a5;text-decoration:none;font-size:13px">Đăng xuất</a>
            </nav>
        </aside>

        <main class="main">
            <header class="topbar">
                <div>
                    <div class="eyebrow">Sinh viên · Kỳ thực tập 2026</div>
                    <h1 id="page-title">Hành trình của tôi</h1>
                    <p class="subtle">Lịch trình, bài tập và kết quả của bạn trong một nơi.</p>
                </div>
                <div class="avatar">
                    <span class="avatar-mark">S</span>Sinh viên thực tập
                </div>
            </header>

            <!-- SECTION 1: HOME -->
            <section id="student-home" class="view active">
                <input id="student-id" type="hidden" value="${studentId}">

                <div class="panel" style="margin-top:18px;">
                    <div class="panel-head">
                        <h2>Lịch trình thực tập</h2>
                    </div>
                    <div style="display:flex;justify-content:space-between;align-items:center;gap:12px;margin-bottom:12px;flex-wrap:wrap;">
                        <button type="button" class="btn secondary" id="calendar-prev">◀</button>
                        <strong id="calendar-month-label">Tháng</strong>
                        <button type="button" class="btn secondary" id="calendar-next">▶</button>
                    </div>
                    <div class="calendar-grid" id="student-calendar"></div>
                    <div id="calendar-detail-panel" class="panel calendar-detail-panel" tabindex="-1" aria-live="polite">
                        <div class="panel-head">
                            <div class="calendar-detail-heading"><span class="calendar-detail-icon" aria-hidden="true">↳</span><h3 id="calendar-detail-title">Chi tiết ngày</h3></div>
                            <span class="calendar-detail-label">Ngày đã chọn</span>
                        </div>
                        <ul id="calendar-detail-list" class="list">
                            <li class="subtle">Chọn một ngày trong lịch để xem lịch trình hoặc thời hạn mở bài.</li>
                        </ul>
                    </div>
                </div>
            </section>

            <!-- SECTION 2: SUBMIT ASSIGNMENTS -->
            <section id="student-submit" class="view">
                <div class="panel">
                    <div class="panel-head">
                        <div><div class="eyebrow">Bài tập</div><h2>Nộp bài tập</h2></div>
                    </div>
                    <p class="subtle">Xem các bài tập được giao và nộp bài làm của bạn.</p>
                    <div id="student-file-list" class="student-assignment-list"><p class="subtle">Đang tải bài tập...</p></div>
                </div>
            </section>

            <!-- SECTION 3: TESTS -->
            <section id="student-test" class="view">
                <div class="panel">
                    <div class="panel-head">
                        <div><div class="eyebrow">Bài tập</div><h2>Làm bài kiểm tra</h2></div>
                        <span class="badge">Trắc nghiệm &amp; lập trình</span>
                    </div>
                    <p class="subtle">Chọn hình thức kiểm tra để xem bài được giao.</p>
                    <div class="assignment-mode-buttons test-mode-buttons" role="tablist" aria-label="Hình thức kiểm tra">
                        <button class="btn secondary assignment-mode-button active" data-test-mode="quiz" type="button" role="tab" aria-selected="true">Trắc nghiệm</button>
                        <button class="btn secondary assignment-mode-button" data-test-mode="code" type="button" role="tab" aria-selected="false">Kiểm tra bằng code</button>
                    </div>
                    <div class="assignment-mode-panel" data-test-panel="quiz" role="tabpanel">
                        <div id="student-quiz-list" class="student-assignment-list"><p class="subtle">Đang tải bài kiểm tra...</p></div>
                    </div>
                    <div class="assignment-mode-panel" data-test-panel="code" role="tabpanel" hidden>
                        <div id="student-code-list" class="student-assignment-list"><p class="subtle">Đang tải bài kiểm tra...</p></div>
                    </div>
                </div>
            </section>

            <!-- SECTION 4: MATERIALS -->
            <section id="student-materials" class="view">
                <div class="panel">
                    <div class="panel-head">
                        <h2>Giáo trình & tài liệu học tập</h2>
                        <span class="badge">Download</span>
                    </div>
                    <p class="subtle">Danh sách tài liệu hỗ trợ học tập, hướng dẫn thực tập và bài giảng của khóa học.</p>
                    <div id="student-material-list" class="material-list"></div>
                </div>
            </section>

            <!-- SECTION 5: MESSAGE -->
            <section id="student-message" class="view">
                <div class="panel">
                    <div class="panel-head">
                        <h2>Trao đổi với mentor</h2>
                        <span class="subtle" id="student-chat-mentor">Đang tải mentor...</span>
                    </div>
                    <div id="student-chat-messages" class="message-box"><div class="subtle">Đang tải cuộc trao đổi...</div></div>
                    <form id="student-chat-form" class="form-actions">
                        <input id="student-chat-input" placeholder="Nhắn cho mentor hướng dẫn..." aria-label="Nội dung tin nhắn" maxlength="2000" required>
                        <button class="btn" type="submit">Gửi tin nhắn</button>
                    </form>
                </div>
            </section>
        </main>
    </div>

    <div id="toast" class="toast"></div>
    <div id="code-exam-overlay" class="code-exam-overlay" role="dialog" aria-modal="true" aria-labelledby="code-exam-title" hidden>
        <div class="code-exam-shell">
            <div class="code-exam-header"><span id="exam-mode-label">Đang kiểm tra</span><span id="code-exam-title"></span></div>
            <div id="code-exam-content"></div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
    <script src="/js/portal.js?v=exam-duration-20261017"></script>
    <script src="/js/realtime.js"></script>
</body>
</html>
