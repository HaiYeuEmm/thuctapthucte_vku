package com.example.ThucTapThucTe.Controller;

import com.example.ThucTapThucTe.Entity.User;
import com.example.ThucTapThucTe.Entity.UserRole;
import com.example.ThucTapThucTe.Service.usersservice;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PageController {

    private static final String SESSION_USER = "loggedInUser";
    private static final String SESSION_ROLE = "loggedInRole";
    private static final String SESSION_USER_ID = "loggedInUserId";

    private final usersservice usersService;

    public PageController(usersservice usersService) {
        this.usersService = usersService;
    }

    @GetMapping({"/", "/login"})
    public String loginPage(HttpSession session) {
        if (session.getAttribute(SESSION_USER) != null) {
            return redirectByRole((String) session.getAttribute(SESSION_ROLE));
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam(defaultValue = "ADMIN") String role,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        if (username.isBlank() || password.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập đầy đủ thông tin đăng nhập.");
            return "redirect:/login";
        }
        UserRole requestedRole;
        try {
            requestedRole = UserRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vai trò không hợp lệ.");
            return "redirect:/login";
        }

        User user = usersService.authenticate(username.trim(), password)
                .orElse(null);
        
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email hoặc mật khẩu không chính xác.");
            return "redirect:/login";
        }

        if (user.getRole() != requestedRole) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tài khoản không thuộc vai trò bạn chọn.");
            return "redirect:/login";
        }

        session.setAttribute(SESSION_USER, user.getEmail());
        session.setAttribute(SESSION_ROLE, user.getRole().name());
        session.setAttribute(SESSION_USER_ID, user.getId());
        return redirectByRole(user.getRole().name());
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/admin")
    public String adminPage(HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        if (!"ADMIN".equals(session.getAttribute(SESSION_ROLE))) {
            return redirectByRole((String) session.getAttribute(SESSION_ROLE));
        }
        return "admin";
    }

    @GetMapping("/mentor")
    public String mentorPage(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        if (!"MENTOR".equals(session.getAttribute(SESSION_ROLE))) {
            return redirectByRole((String) session.getAttribute(SESSION_ROLE));
        }
        model.addAttribute("mentorId", session.getAttribute(SESSION_USER_ID));
        return "mentor";
    }

    @GetMapping("/student")
    public String studentPage(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        if (!"STUDENT".equals(session.getAttribute(SESSION_ROLE))) {
            return redirectByRole((String) session.getAttribute(SESSION_ROLE));
        }
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            String email = (String) session.getAttribute(SESSION_USER);
            userId = usersService.findByEmail(email).map(User::getId).orElse(null);
            if (userId != null) {
                session.setAttribute(SESSION_USER_ID, userId);
            }
        }
        model.addAttribute("studentId", userId);
        return "student";
    }

    @GetMapping("/chat")
    public String chatPage(HttpSession session, Model model) {
        if (!isLoggedIn(session)) {
            return "redirect:/login";
        }
        Object userId = session.getAttribute(SESSION_USER_ID);
        String role = (String) session.getAttribute(SESSION_ROLE);
        model.addAttribute("currentUserId", userId != null ? userId : 0L);
        model.addAttribute("currentRole", role != null ? role : "MENTOR");
        return "chat";
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute(SESSION_USER) != null;
    }

    private String redirectByRole(String role) {
        return switch (role) {
            case "MENTOR" -> "redirect:/mentor";
            case "STUDENT" -> "redirect:/student";
            default -> "redirect:/admin";
        };
    }
}
