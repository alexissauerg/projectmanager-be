package com.projectmanager.controller;

import com.projectmanager.dto.user.UserReadDto;
import com.projectmanager.dto.user.UserUpdateDto;
import com.projectmanager.entity.Role;
import com.projectmanager.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) authentication.getPrincipal();
    }

    private Role getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> Role.valueOf(authority.getAuthority().replace("ROLE_", "")))
                .orElseThrow(() -> new RuntimeException("No role found for user"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserReadDto> getUserById(@PathVariable UUID id) {
        UUID currentUserId = getCurrentUserId();
        Role currentUserRole = getCurrentUserRole();
        UserReadDto user = userService.getUserById(id, currentUserId, currentUserRole);
        logger.info("User retrieved with ID: {}", id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserReadDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean emailVerified) {
        UUID currentUserId = getCurrentUserId();
        Role currentUserRole = getCurrentUserRole();
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<UserReadDto> users = userService.getAllUsers(pageRequest, name, email, role, emailVerified, currentUserId, currentUserRole);
        logger.info("All users retrieved by admin");
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserReadDto> updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateDto dto) {
        UUID currentUserId = getCurrentUserId();
        Role currentUserRole = getCurrentUserRole();
        UserReadDto updatedUser = userService.updateUser(id, dto, currentUserId, currentUserRole);
        logger.info("User updated with ID: {}", id);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        UUID currentUserId = getCurrentUserId();
        Role currentUserRole = getCurrentUserRole();
        userService.deleteUser(id, currentUserId, currentUserRole);
        logger.info("User deleted with ID: {}", id);
        return ResponseEntity.ok().build();
    }

}
