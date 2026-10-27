<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý thực tập | Admin</title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body>
<div class="shell">
    <aside class="sidebar"><div class="brand">INTERNSHIP OS<small>Quản trị vận hành</small></div><nav class="nav">
        <button class="active" data-view="overview" data-title="Tổng quan"><span>01</span>Tổng quan</button>
        <button data-view="classes" data-title="Lớp thực tập"><span>02</span>Lớp thực tập</button>
        <button data-view="people" data-title="Người dùng"><span>03</span>Người dùng</button>
        <button data-view="operations" data-title="Vận hành đào tạo"><span>04</span>Vận hành đào tạo</button>
        <a href="/logout" style="margin-top:28px;padding:12px;color:#f0a5a5;text-decoration:none;font-size:13px">Đăng xuất</a>
    </nav></aside>
    <main class="main">
        <header class="topbar"><div><div class="eyebrow">Năm 2026 · Kỳ thực tập</div><h1 id="page-title">Tổng quan</h1><p class="subtle">Theo dõi tiến độ và phân công trong một màn hình.</p></div><div class="avatar"><span class="avatar-mark">A</span>Quản trị viên</div></header>
        <section id="overview" class="view active"><div class="metrics">
            <div class="metric"><div class="metric-accent"></div><div class="metric-label">Lớp thực tập</div><div id="class-count" class="metric-value">—</div></div>
            <div class="metric"><div class="metric-accent" style="background:#0f766e"></div><div class="metric-label">Mentor</div><div id="mentor-count" class="metric-value">—</div></div>
            <div class="metric"><div class="metric-accent" style="background:#3b82f6"></div><div class="metric-label">Sinh viên</div><div id="student-count" class="metric-value">—</div></div>
            <div class="metric"><div class="metric-accent" style="background:#805ad5"></div><div class="metric-label">Trạng thái hệ thống</div><div class="metric-value" style="font-size:20px">Đang chạy</div></div>
        </div><div class="panel"><div class="panel-head"><h2>Lớp thực tập gần đây</h2><button class="btn secondary" data-view="classes" data-title="Lớp thực tập">Xem toàn bộ</button></div><div class="table-wrap"><table><thead><tr><th>Tên lớp</th><th>Kỳ</th><th>Mentor</th><th>Sinh viên</th></tr></thead><tbody id="overview-class-table"><tr><td colspan="4">Đang tải dữ liệu...</td></tr></tbody></table></div></div></section>
        <section id="classes" class="view"><div class="panel" style="margin-bottom:18px"><div class="panel-head"><h2>Tạo lớp thực tập</h2><span class="subtle">Phân loại theo kỳ, bộ phận và chuyên ngành</span></div><form id="class-form"><div class="form-grid"><div><label>Tên lớp</label><input name="name" placeholder="Ví dụ: Fullstack Web 01" required></div><div><label>Kỳ thực tập</label><input name="term" placeholder="Kỳ hè 2026" required></div><div><label>Bộ phận</label><input name="department" placeholder="Công nghệ thông tin" required></div><div><label>Chuyên ngành</label><input name="major" placeholder="Phát triển phần mềm" required></div></div><div class="form-actions"><button class="btn" type="submit">Tạo lớp</button></div></form></div><div class="panel"><div class="panel-head"><h2>Danh sách lớp</h2></div><div class="table-wrap"><table><thead><tr><th>Tên lớp</th><th>Kỳ</th><th>Mentor</th><th>Sinh viên</th></tr></thead><tbody id="class-table"></tbody></table></div></div></section>
        <section id="people" class="view"><div class="panel" style="margin-bottom:18px"><div class="panel-head"><h2>Mentor hướng dẫn</h2></div><div class="table-wrap"><table><thead><tr><th>Mã</th><th>Họ tên</th><th>Email</th></tr></thead><tbody id="mentor-table"><tr><td colspan="3">Đang tải dữ liệu...</td></tr></tbody></table></div></div><div class="panel"><div class="panel-head"><h2>Sinh viên thực tập</h2></div><div class="table-wrap"><table><thead><tr><th>Mã</th><th>Họ tên</th><th>Email</th></tr></thead><tbody id="student-table"><tr><td colspan="3">Đang tải dữ liệu...</td></tr></tbody></table></div></div></section>
        <section id="operations" class="view">
            <div class="panel">
                <div class="panel-head">
                    <h2>Vận hành đào tạo</h2>
                    <span id="operations-status" class="badge">Đang tải dữ liệu...</span>
                </div>
                <div class="metrics">
                    <div class="metric"><div class="metric-label">Lịch trình</div><div id="operations-schedule-count" class="metric-value">—</div></div>
                    <div class="metric"><div class="metric-label">Bài tập</div><div id="operations-assignment-count" class="metric-value">—</div></div>
                    <div class="metric"><div class="metric-label">Lượt điểm danh</div><div id="operations-attendance-count" class="metric-value">—</div></div>
                    <div class="metric"><div class="metric-label">Đánh giá</div><div id="operations-evaluation-count" class="metric-value">—</div></div>
                </div>
                <p id="operations-description" class="subtle">Tổng hợp dữ liệu đào tạo từ tất cả lớp thực tập.</p>
            </div>
        </section>
    </main>
</div><div id="toast" class="toast"></div><script src="/js/app.js?v=admin-operations-20261017"></script>
</body>
</html>
