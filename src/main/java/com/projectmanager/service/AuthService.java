package com.projectmanager.service;

import com.projectmanager.dto.auth.LoginDto;
import com.projectmanager.dto.auth.PasswordResetDto;
import com.projectmanager.dto.auth.PasswordResetRequestDto;
import com.projectmanager.dto.auth.RefreshTokenDto;
import com.projectmanager.dto.auth.TokenResponseDto;
import com.projectmanager.entity.RefreshToken;
import com.projectmanager.entity.User;
import com.projectmanager.exception.NotFoundException;
import com.projectmanager.exception.UnauthorizedException;
import com.projectmanager.repository.RefreshTokenRepository;
import com.projectmanager.security.JwtTokenProvider;
import com.projectmanager.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final Map<String, String> resetTokenStore = new HashMap<>(); // Temporary in-memory store for reset tokens

    public AuthService(UserService userService, JwtTokenProvider jwtTokenProvider, 
                       RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, 
                       EmailService emailService) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public TokenResponseDto login(LoginDto dto) {
        User user = userService.getUserEntityByEmail(dto.getEmail());

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Email not verified");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(refreshToken)
                .user(user)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(refreshTokenEntity);
        logger.info("User logged in with ID: {}", user.getId());

        TokenResponseDto response = new TokenResponseDto();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        return response;
    }

    public TokenResponseDto refreshToken(RefreshTokenDto dto) {
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(dto.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshTokenEntity.isRevoked() || refreshTokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        User user = refreshTokenEntity.getUser();
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        refreshTokenEntity.setRevoked(true);
        refreshTokenRepository.save(refreshTokenEntity);

        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token(newRefreshToken)
                .user(user)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(newRefreshTokenEntity);
        logger.info("Token refreshed for user ID: {}", user.getId());

        TokenResponseDto response = new TokenResponseDto();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        return response;
    }

    public void logout(String refreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new NotFoundException("Refresh token not found"));

        refreshTokenEntity.setRevoked(true);
        refreshTokenRepository.save(refreshTokenEntity);
        logger.info("User logged out, refresh token revoked");
    }

    public void requestPasswordReset(PasswordResetRequestDto dto, String baseUrl) throws Exception {
        User user = userService.getUserEntityByEmail(dto.getEmail());
        String resetToken = RandomUtil.generateRandomString(32);
        resetTokenStore.put(resetToken, user.getEmail()); // Store token temporarily
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken, baseUrl);
        logger.info("Password reset email sent to: {}", user.getEmail());
    }

    public void resetPassword(PasswordResetDto dto) {
        String email = resetTokenStore.get(dto.getToken());
        if (email == null) {
            throw new NotFoundException("Invalid or expired reset token");
        }
        User user = userService.getUserEntityByEmail(email);
        userService.updateUserPassword(user.getId(), dto.getNewPassword());
        logger.info("Password reset for user ID: {}", user.getId());
        resetTokenStore.remove(dto.getToken()); // Remove token after use
    }
}
