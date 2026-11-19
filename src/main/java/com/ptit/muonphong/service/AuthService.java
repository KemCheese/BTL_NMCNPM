package com.ptit.muonphong.service;

import com.ptit.muonphong.config.CustomUserDetails;
import com.ptit.muonphong.entity.NguoiDung;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public NguoiDung getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        if (auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getNguoiDung();
        }
        return null;
    }

    public boolean isAuthenticated() {
        return getCurrentUser() != null;
    }
}
