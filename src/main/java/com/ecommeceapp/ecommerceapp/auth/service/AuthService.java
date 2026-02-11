package com.ecommeceapp.ecommerceapp.auth.service;


import com.ecommeceapp.ecommerceapp.auth.dto.*;
import com.ecommeceapp.ecommerceapp.common.exception.BadRequestException;
import com.ecommeceapp.ecommerceapp.token.entity.RefreshToken;
import com.ecommeceapp.ecommerceapp.token.service.RefreshTokenService;
import com.ecommeceapp.ecommerceapp.security.JwtService;
import com.ecommeceapp.ecommerceapp.user.entity.Role;
import com.ecommeceapp.ecommerceapp.user.entity.User;
import com.ecommeceapp.ecommerceapp.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public void register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(Set.of(Role.ROLE_USER))
                .build();

        userRepository.save(user);
    }

    public void registerAdmin(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(Set.of(Role.ROLE_ADMIN))
                .build();

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String access = jwtService.generateAccessToken(user.getEmail());
        String refresh = jwtService.generateRefreshToken(user.getEmail());

        Instant refreshExp = jwtService.refreshTokenExpiryInstant();
        refreshTokenService.create(user, refresh, refreshExp);

        return new AuthResponse(access, refresh);
    }

    public AuthResponse refresh(RefreshRequest req) {
        RefreshToken rt = refreshTokenService.verify(req.getRefreshToken());

        String email = rt.getUser().getEmail();

        // rotate refresh token (recommended)
        rt.setRevoked(true); // revoke old
        String newRefresh = jwtService.generateRefreshToken(email);
        refreshTokenService.create(rt.getUser(), newRefresh, jwtService.refreshTokenExpiryInstant());

        String newAccess = jwtService.generateAccessToken(email);
        return new AuthResponse(newAccess, newRefresh);
    }

    public void logout(LogoutRequest req) {
        refreshTokenService.revoke(req.getRefreshToken());
    }

    public void logoutAll(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        refreshTokenService.revokeAllForUser(user.getId());
    }
}
