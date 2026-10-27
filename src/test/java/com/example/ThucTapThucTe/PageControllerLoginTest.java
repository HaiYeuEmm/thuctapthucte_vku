package com.example.ThucTapThucTe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.ThucTapThucTe.Controller.PageController;
import com.example.ThucTapThucTe.Entity.User;
import com.example.ThucTapThucTe.Entity.UserRole;
import com.example.ThucTapThucTe.Service.usersservice;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PageControllerLoginTest {

    @Test
    void adminPageRequiresLogin() {
        usersservice usersService = Mockito.mock(usersservice.class);
        PageController controller = new PageController(usersService);
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.when(session.getAttribute("loggedInUser")).thenReturn(null);

        String result = controller.adminPage(session);

        assertEquals("redirect:/login", result);
    }

    @Test
    void loginCreatesSessionForRole() {
        usersservice usersService = Mockito.mock(usersservice.class);
        PageController controller = new PageController(usersService);
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.when(usersService.authenticate("student@test.com", "123456"))
                .thenReturn(Optional.of(new User("Student", "student@test.com", "123456", UserRole.STUDENT)));

        String result = controller.login("student@test.com", "123456", "STUDENT", session, new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());

        assertEquals("redirect:/student", result);
        Mockito.verify(session).setAttribute("loggedInUser", "student@test.com");
        Mockito.verify(session).setAttribute("loggedInRole", "STUDENT");
    }
}
