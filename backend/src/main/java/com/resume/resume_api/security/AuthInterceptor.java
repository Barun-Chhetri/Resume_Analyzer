package com.resume.resume_api.security;

import com.resume.resume_api.entity.UserEntity;
import com.resume.resume_api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        } else {
            String tokenParam = request.getHeader("X-Auth-Token");
            if (tokenParam != null && !tokenParam.isBlank()) {
                token = tokenParam.trim();
            }
        }

        if (token != null && !token.isBlank()) {
            UUID userId = tokenService.verifyTokenAndGetUserId(token);
            if (userId != null) {
                UserEntity user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    UserContext.setCurrentUser(user);
                }
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
