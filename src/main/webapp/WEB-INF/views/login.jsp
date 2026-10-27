<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập | Thực tập doanh nghiệp</title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body class="login-page">
    <main class="login">
        <section class="intro"><div class="signal"></div><h1>Thực tập có định hướng.</h1><p>Một không gian thống nhất để nhà trường, mentor và sinh viên theo dõi hành trình thực tập từ ngày đầu đến báo cáo cuối kỳ.</p></section>
        <section class="form">
            <div class="eyebrow">Cổng quản lý</div><h2>Chào mừng trở lại</h2><p class="subtle">Đăng nhập để tiếp tục công việc của bạn.</p>
            <% String errorMessage = (String) request.getAttribute("errorMessage"); %>
            <% if (errorMessage != null) { %><div class="error"><%= errorMessage %></div><% } %>
            <form action="/login" method="post">
                <div><label for="username">Email hoặc tài khoản</label><input id="username" name="username" type="text" required autocomplete="username"></div>
                <div><label for="password">Mật khẩu</label><input id="password" name="password" type="password" required autocomplete="current-password"></div>
                <div><label for="role">Vai trò truy cập</label><select id="role" name="role"><option value="ADMIN">Quản trị viên</option><option value="MENTOR">Giảng viên hướng dẫn</option><option value="STUDENT">Sinh viên thực tập</option></select></div>
                <div class="form-actions"><button class="btn" type="submit">Đăng nhập vào hệ thống</button></div>
            </form>
        </section>
    </main>
</body>
</html>
