package com.example.ThucTapThucTe.Repository;

import com.example.ThucTapThucTe.Entity.User;
import com.example.ThucTapThucTe.Entity.UserRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface usersrepository extends JpaRepository<User, Long> {

	List<User> findByRole(UserRole role);
	Optional<User> findByEmailIgnoreCase(String email);
	Optional<User> findByEmailIgnoreCaseAndPassword(String email, String password);
	// @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.password = :password")
	// Optional<User> findByEmailIgnoreCaseAndPassword(String email, String password);
	boolean existsByEmailIgnoreCase(String email);
}
