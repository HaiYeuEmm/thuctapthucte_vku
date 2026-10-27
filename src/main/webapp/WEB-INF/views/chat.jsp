<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chat nhóm | Internship OS</title><link rel="stylesheet" href="/css/app.css"><link rel="stylesheet" href="/css/chat.css">
</head>
<body class="chat-page">
<div class="chat-shell">
    <aside class="chat-sidebar">
        <div class="chat-brand">INTERNSHIP OS<small>Trao đổi theo nhóm</small></div>
        <div class="chat-settings">
            <div><label for="current-role">Vai trò hiện tại</label><select id="current-role">
                <option value="MENTOR" ${currentRole == 'MENTOR' ? 'selected' : ''}>Mentor</option>
                <option value="STUDENT" ${currentRole == 'STUDENT' ? 'selected' : ''}>Sinh viên</option>
            </select></div>
            <div><label for="current-user-id">Mã người dùng</label><input id="current-user-id" type="number" min="1" value="${currentUserId}" readonly></div>
            <div><label for="current-class-id">Mã lớp chat</label><input id="current-class-id" type="number" min="1" placeholder="Lớp phụ trách"></div>
        </div>
        <div class="chat-groups-title"><span>Nhóm của tôi</span><button id="create-group" class="btn" style="padding:5px 9px;font-size:11px">+ Nhóm</button></div>
        <div id="groups" class="group-list"></div>
        <a href="/logout" style="display:block;margin-top:28px;padding:10px;color:#f0a5a5;text-decoration:none;font-size:13px">Đăng xuất</a>
    </aside>
    <main class="chat-main">
        <header class="chat-header"><div><h1 id="group-name" class="chat-title">Chọn một nhóm để bắt đầu</h1><div id="group-meta" class="subtle" style="margin-top:5px">Tin nhắn realtime giữa mentor và sinh viên</div></div><div class="chat-status"><span id="connection-dot" class="status-dot"></span><span id="connection-label">Đang kết nối...</span></div></header>
        <section id="messages" class="messages"><div id="chat-empty" class="chat-empty">Chọn một nhóm ở bên trái để xem lịch sử và trao đổi trực tiếp.</div></section>
        <form id="message-form" class="chat-composer"><textarea id="message-input" placeholder="Viết tin nhắn cho nhóm..." maxlength="2000" required></textarea><button class="btn" type="submit">Gửi</button></form>
    </main>
    <aside class="chat-info"><h3>Thành viên nhóm</h3><ul id="member-list" class="member-list"><li class="subtle">Chưa chọn nhóm</li></ul><div id="mentor-tools"><h3>Quản lý nhóm</h3><form id="member-form"><label for="student-to-add">Thêm sinh viên</label><select id="student-to-add" name="studentId" required><option>Đang tải...</option></select><button class="btn" type="submit">Thêm vào nhóm</button></form></div></aside>
</div>
<div id="group-modal" class="chat-modal"><div class="chat-modal-card"><h2>Tạo nhóm chat</h2><p class="subtle">Nhóm mới phải thuộc một lớp do Mentor hiện tại phụ trách.</p><form id="group-form"><label for="group-class-input">Mã lớp</label><input id="group-class-input" name="classId" type="number" min="1" required><label for="group-name-input">Tên nhóm</label><input id="group-name-input" name="name" maxlength="80" placeholder="Ví dụ: Fullstack Web · Nhóm 01" required><div class="chat-modal-actions"><button id="cancel-group" class="btn secondary" type="button">Hủy</button><button class="btn" type="submit">Tạo nhóm</button></div></form></div></div>
<div id="chat-toast" class="toast"></div>
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"></script><script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script><script src="/js/chat.js"></script>
</body></html>
