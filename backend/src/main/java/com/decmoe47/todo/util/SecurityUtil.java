package com.decmoe47.todo.util;

import com.decmoe47.todo.exception.AuthenticationException;
import com.decmoe47.todo.model.entity.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityUtil {

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static Long getCurrentUserId() {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user.getId();
            }
        }
        throw new AuthenticationException("Failed to get current user id");
    }
}
