package com.projectmanager.listener;

import com.projectmanager.entity.Role;
import com.projectmanager.entity.User;
import com.projectmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.admin.name:Admin User}")
    private String adminName;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    public ApplicationStartupListener(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        if (userRepository.count() == 0) {
            User adminUser = User.builder()
                    .id(UUID.randomUUID())
                    .email(adminEmail)
                    .name(adminName)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .emailVerified(true)
                    .deleted(false)
                    .build();

            userRepository.save(adminUser);
            logger.info("Admin user created with email: {}", adminEmail);
        } else {
            logger.info("Users already exist, skipping admin user creation");
        }
    }

}
