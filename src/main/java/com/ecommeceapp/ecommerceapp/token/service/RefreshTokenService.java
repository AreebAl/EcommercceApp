package com.ecommeceapp.ecommerceapp.token.service;


import com.ecommeceapp.ecommerceapp.common.exception.BadRequestException;
import com.ecommeceapp.ecommerceapp.token.entity.RefreshToken;
import com.ecommeceapp.ecommerceapp.token.repo.RefreshTokenRepository;
import com.ecommeceapp.ecommerceapp.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken create(User user, String token, Instant expiresAt) {
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();
        return refreshTokenRepository.save(rt);
    }

    public RefreshToken verify(String token) {
        RefreshToken rt = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (rt.isRevoked()) {
            throw new BadRequestException("Refresh token revoked");
        }
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Refresh token expired");
        }
        return rt;
    }

    public void revoke(String token) {
        RefreshToken rt = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);
    }

    public void revokeAllForUser(Long userId) {
        // quick approach: delete all tokens for user
        refreshTokenRepository.deleteByUserId(userId);
    }
}
