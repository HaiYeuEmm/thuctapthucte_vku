package com.example.ThucTapThucTe.Controller;

import com.example.ThucTapThucTe.Dto.usersdto;
import com.example.ThucTapThucTe.Entity.User;
import com.example.ThucTapThucTe.Entity.UserRole;
import com.example.ThucTapThucTe.Service.usersservice;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class userscontroller {

	private final usersservice usersService;

	public userscontroller(usersservice usersService) {
		this.usersService = usersService;
	}

	@GetMapping
	public List<usersdto> getUsers(@RequestParam(required = false) UserRole role) {
		List<User> users = usersService.findAll(role);
		return users.stream().map(usersdto::from).toList();
	}

	@GetMapping("/{id}")
	public usersdto getUser(@PathVariable Long id) {
		return usersdto.from(usersService.findById(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public usersdto createUser(@RequestBody CreateUserRequest request) {
		return usersdto.from(usersService.create(request.fullName(), request.email(), request.password(), request.role()));
	}

	public record CreateUserRequest(String fullName, String email, String password, UserRole role) {
	}
}
