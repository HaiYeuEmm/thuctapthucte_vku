package com.example.ThucTapThucTe.Service;

import com.example.ThucTapThucTe.Entity.User;
import com.example.ThucTapThucTe.Entity.UserRole;
import com.example.ThucTapThucTe.Repository.usersrepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class usersservice {

	private final usersrepository repository;

	public usersservice(usersrepository repository) {
		this.repository = repository;
	}

	public List<User> findAll(UserRole role) {
		return role == null ? repository.findAll() : repository.findByRole(role);
	}

	public User findById(Long id) {
		return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Khong tim thay nguoi dung voi ma " + id));
	}

	public Optional<User> findByEmail(String email) {
		return repository.findByEmailIgnoreCase(email);
	}

	public Optional<User> authenticate(String email, String password) {
		if (email == null || email.isBlank() || password == null || password.isBlank()) {
			return Optional.empty();
		}
		return repository.findByEmailIgnoreCaseAndPassword(email.trim().toLowerCase(), password);
	}

	public User create(String fullName, String email, UserRole role) {
		return create(fullName, email, null, role);
	}

	public User create(String fullName, String email, String password, UserRole role) {
		if (fullName == null || fullName.isBlank()) {
			throw new IllegalArgumentException("Ho ten khong duoc de trong");
		}
		if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
			throw new IllegalArgumentException("Email khong hop le");
		}
		if (repository.existsByEmailIgnoreCase(email.trim())) {
			throw new IllegalArgumentException("Email da ton tai trong he thong");
		}
		if (role == null) {
			throw new IllegalArgumentException("Vai tro khong duoc de trong");
		}
		if (password == null || password.isBlank()) {
			throw new IllegalArgumentException("Mat khau khong duoc de trong");
		}
		return repository.save(new User(fullName.trim(), email.trim().toLowerCase(), password, role));
	}

	public User requireRole(Long id, UserRole role) {
		User user = findById(id);
		if (user.getRole() != role) {
			throw new IllegalArgumentException("Nguoi dung khong co vai tro " + role);
		}
		return user;
	}
}
