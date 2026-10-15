package com.resume.resume_api.security;

import com.resume.resume_api.entity.UserEntity;
import com.resume.resume_api.exception.InvalidInputException;

public class UserContext {

    private static final ThreadLocal<UserEntity> CURRENT_USER = new ThreadLocal<>();

    public static void setCurrentUser(UserEntity user) {
        CURRENT_USER.set(user);
    }

    public static UserEntity getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static UserEntity getRequiredCurrentUser() {
        UserEntity user = CURRENT_USER.get();
        if (user == null) {
            throw new InvalidInputException("Authentication required. Please sign in to access this resource.");
        }
        return user;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
