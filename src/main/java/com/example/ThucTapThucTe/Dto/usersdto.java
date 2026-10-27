package com.example.ThucTapThucTe.Dto;

import com.example.ThucTapThucTe.Entity.User;
import com.example.ThucTapThucTe.Entity.UserRole;

public class usersdto {
	private Long id;
	private String fullName;
	private String email;
	private UserRole role;

	public usersdto() {
	}

	public usersdto(Long id, String fullName, String email, UserRole role) {
		this.id = id;
		this.fullName = fullName;
		this.email = email;
		this.role = role;
	}

	public static usersdto from(User user) {
		return new usersdto(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
	}

	public Long getId() { return id; }
	public String getFullName() { return fullName; }
	public String getEmail() { return email; }
	public UserRole getRole() { return role; }
}
