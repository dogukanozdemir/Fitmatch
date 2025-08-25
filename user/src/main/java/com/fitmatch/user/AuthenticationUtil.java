package com.fitmatch.user;

import com.fitmatch.user.dto.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationUtil {

    public AuthenticatedUser getAuthenticatedCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        assert principal instanceof User;
        User user = (User) principal;
        return AuthenticatedUser.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }
}
