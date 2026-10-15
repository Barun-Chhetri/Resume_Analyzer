package com.resume.resume_api.service;

import com.resume.resume_api.dto.auth.AuthResponse;
import com.resume.resume_api.dto.auth.LoginRequest;
import com.resume.resume_api.dto.auth.RegisterRequest;
import com.resume.resume_api.dto.auth.UserDTO;
import com.resume.resume_api.entity.UserEntity;
import com.resume.resume_api.exception.InvalidInputException;
import com.resume.resume_api.repository.UserRepository;
import com.resume.resume_api.security.PasswordUtil;
import com.resume.resume_api.security.TokenService;
import com.resume.resume_api.security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidInputException("Name is required.");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty() || !request.getEmail().contains("@")) {
            throw new InvalidInputException("A valid email address is required.");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new InvalidInputException("Password must be at least 6 characters long.");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new InvalidInputException("An account with this email already exists. Please sign in instead.");
        }

        String passwordHash = PasswordUtil.hashPassword(request.getPassword());

        UserEntity user = UserEntity.builder()
                .name(request.getName().trim())
                .email(normalizedEmail)
                .passwordHash(passwordHash)
                .build();

        user = userRepository.save(user);
        log.info("Registered new user with id: {} and email: {}", user.getId(), user.getEmail());

        String token = tokenService.generateToken(user.getId());
        return AuthResponse.builder()
                .token(token)
                .user(mapToDTO(user))
                .message("Account created successfully.")
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new InvalidInputException("Email is required.");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new InvalidInputException("Password is required.");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        UserEntity user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidInputException("Invalid email or password."));

        if (!PasswordUtil.verifyPassword(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidInputException("Invalid email or password.");
        }

        String token = tokenService.generateToken(user.getId());
        log.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .user(mapToDTO(user))
                .message("Logged in successfully.")
                .build();
    }

    public UserDTO getCurrentUser() {
        UserEntity user = UserContext.getRequiredCurrentUser();
        return mapToDTO(user);
    }

    private UserDTO mapToDTO(UserEntity entity) {
        return UserDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
