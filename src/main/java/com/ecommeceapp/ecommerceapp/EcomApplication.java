// package com.ecommeceapp.ecommerceapp;

// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication
// public class EcommerceappApplication {

// 	public static void main(String[] args) {
// 		SpringApplication.run(EcommerceappApplication.class, args);
// 	}

// }



package com.ecommeceapp.ecommerceapp;

import com.ecommeceapp.ecommerceapp.user.entity.Role;
import com.ecommeceapp.ecommerceapp.user.entity.User;
import com.ecommeceapp.ecommerceapp.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@SpringBootApplication
@RequiredArgsConstructor
public class EcomApplication {

  public static void main(String[] args) {
    SpringApplication.run(EcomApplication.class, args);
  }

  @Bean
  CommandLineRunner seedAdmin(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      @Value("${appseed.admin.name}") String name,
      @Value("${appseed.admin.email}") String email,
      @Value("${appseed.admin.password}") String password
  ) {
    return args -> {
      if (!userRepository.existsByEmail(email)) {
        User admin = User.builder()
            .name(name)
            .email(email)
            .password(passwordEncoder.encode(password))
            .roles(Set.of(Role.ROLE_ADMIN))
            .build();
        userRepository.save(admin);
      }
    };
  }
}
