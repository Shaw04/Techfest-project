package com.example.TechFest.data.User1;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.TechFest.data.Sponser.Sponser;
@Repository
public interface UserRepo extends JpaRepository<User1, Long> {
	Optional<User1> findByUsername(String Username);
}
