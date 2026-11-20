package com.personalfinance.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Tiện ích lấy thông tin định danh người dùng hiện tại an toàn từ Security Context
 */
public final class SecurityUtils {

    public static final UUID DEFAULT_ADMIN_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private SecurityUtils() {
    }

    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equalsIgnoreCase(String.valueOf(auth.getPrincipal()))) {
            return "admin";
        }
        return auth.getName();
    }

    public static UUID getCurrentUserId() {
        String username = getCurrentUsername();
        if ("admin".equalsIgnoreCase(username)) {
            return DEFAULT_ADMIN_USER_ID;
        }
        return UUID.nameUUIDFromBytes(username.getBytes(StandardCharsets.UTF_8));
    }
}
