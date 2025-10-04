package com.projectmanager.controller;

import com.projectmanager.dto.auth.LoginDto;
import com.projectmanager.dto.auth.PasswordResetDto;
import com.projectmanager.dto.auth.PasswordResetRequestDto;
import com.projectmanager.dto.auth.RefreshTokenDto;
import com.projectmanager.dto.auth.TokenResponseDto;
import com.projectmanager.dto.user.UserCreateDto;
import com.projectmanager.dto.user.UserReadDto;
import com.projectmanager.service.AuthService;
import com.projectmanager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserReadDto> register(@Valid @RequestBody UserCreateDto dto, HttpServletRequest request) throws Exception {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        UserReadDto createdUser = userService.createUser(dto, baseUrl);
        logger.info("User registered with email: {}", dto.getEmail());
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginDto dto) {
        TokenResponseDto tokens = authService.login(dto);
        logger.info("User logged in with email: {}", dto.getEmail());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refreshToken(@Valid @RequestBody RefreshTokenDto dto) {
        TokenResponseDto tokens = authService.refreshToken(dto);
        logger.info("Token refreshed");
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenDto dto) {
        authService.logout(dto.getRefreshToken());
        logger.info("User logged out");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify")
    public ResponseEntity<UserReadDto> verifyEmail(@RequestParam String token) {
        UserReadDto verifiedUser = userService.verifyEmail(token);
        logger.info("Email verified for user");
        return ResponseEntity.ok(verifiedUser);
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto dto, HttpServletRequest request) throws Exception {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        authService.requestPasswordReset(dto, baseUrl);
        logger.info("Password reset requested for email: {}", dto.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetDto dto) {
        authService.resetPassword(dto);
        logger.info("Password reset completed");
        return ResponseEntity.ok().build();
    }

}
