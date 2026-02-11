package com.ecommeceapp.ecommerceapp.user.repo;

import com.ecommeceapp.ecommerceapp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByEmail(String email);
  Optional<User> findByEmail(String email);
    
} 
