package com.projectmanager.service;

import com.projectmanager.dto.user.UserCreateDto;
import com.projectmanager.dto.user.UserReadDto;
import com.projectmanager.dto.user.UserUpdateDto;
import com.projectmanager.entity.Role;
import com.projectmanager.entity.User;
import com.projectmanager.exception.BadRequestException;
import com.projectmanager.exception.NotFoundException;
import com.projectmanager.exception.UnauthorizedException;
import com.projectmanager.mapper.UserMapper;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.specification.UserSpecification;
import com.projectmanager.util.RandomUtil;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final Map<String, String> verificationTokenStore = new HashMap<>(); // Temporary in-memory store for verification tokens

    public UserService(UserRepository userRepository, UserMapper userMapper, 
                       PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public UserReadDto createUser(UserCreateDto dto, String baseUrl) throws MessagingException {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = userMapper.toEntity(dto);
        user.setId(UUID.randomUUID());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        user.setEmailVerified(false);
        user.setDeleted(false);

        User savedUser = userRepository.save(user);
        logger.info("User created with ID: {}", savedUser.getId());

        String verificationToken = RandomUtil.generateRandomString(32);
        verificationTokenStore.put(verificationToken, savedUser.getEmail());
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken, baseUrl);
        logger.info("Verification email sent to: {}", savedUser.getEmail());

        return userMapper.toReadDto(savedUser);
    }

    public UserReadDto verifyEmail(String token) {
        String email = verificationTokenStore.get(token);
        if (email == null) {
            throw new NotFoundException("Invalid or expired verification token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        user.setEmailVerified(true);
        User updatedUser = userRepository.save(user);
        verificationTokenStore.remove(token);
        logger.info("Email verified for user ID: {}", updatedUser.getId());

        return userMapper.toReadDto(updatedUser);
    }

    public UserReadDto getUserById(UUID id, UUID currentUserId, Role currentUserRole) {
        if (!currentUserId.equals(id) && currentUserRole != Role.ADMIN) {
            throw new UnauthorizedException("You are not authorized to view this user");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));

        return userMapper.toReadDto(user);
    }

    public Page<UserReadDto> getAllUsers(Pageable pageable, String name, String email, 
                                         String role, Boolean emailVerified, 
                                         UUID currentUserId, Role currentUserRole) {
        if (currentUserRole != Role.ADMIN) {
            throw new UnauthorizedException("Only admins can view all users");
        }

        var spec = UserSpecification.hasNameLike(name)
                .and(UserSpecification.hasEmailLike(email))
                .and(UserSpecification.hasRole(role))
                .and(UserSpecification.isEmailVerified(emailVerified));

        return userRepository.findAll(spec, pageable)
                .map(userMapper::toReadDto);
    }

    public UserReadDto updateUser(UUID id, UserUpdateDto dto, UUID currentUserId, Role currentUserRole) {
        if (!currentUserId.equals(id) && currentUserRole != Role.ADMIN) {
            throw new UnauthorizedException("You are not authorized to update this user");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));

        userMapper.updateEntity(user, dto);
        User updatedUser = userRepository.save(user);
        logger.info("User updated with ID: {}", updatedUser.getId());

        return userMapper.toReadDto(updatedUser);
    }

    public void deleteUser(UUID id, UUID currentUserId, Role currentUserRole) {
        if (!currentUserId.equals(id) && currentUserRole != Role.ADMIN) {
            throw new UnauthorizedException("You are not authorized to delete this user");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));

        user.setDeleted(true);
        userRepository.save(user);
        logger.info("User logically deleted with ID: {}", id);
    }

    public User getUserEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
    }

    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    public void updateUserPassword(UUID id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("User password updated for ID: {}", id);
    }

}
