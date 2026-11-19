package com.personalfinance.security;

import com.personalfinance.common.exception.BusinessException;
import com.personalfinance.common.response.ApiResponse;
import com.personalfinance.security.dto.AuthResponse;
import com.personalfinance.security.dto.LoginRequest;
import com.personalfinance.security.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller xử lý đăng nhập và xác thực phiên người dùng
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    @Value("${app.security.admin-username:admin}")
    private String adminUsername;

    @Value("${app.security.admin-password:admin123}")
    private String adminPassword;

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        if (!adminUsername.equals(request.getUsername()) || !adminPassword.equals(request.getPassword())) {
            throw new BusinessException("Tên đăng nhập hoặc mật khẩu không chính xác", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtUtil.generateToken(request.getUsername());
        UserResponse userResponse = UserResponse.builder()
                .username(request.getUsername())
                .role("ROLE_ADMIN")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(jwtUtil.getExpirationMs())
                .username(request.getUsername())
                .user(userResponse)
                .build();

        return ApiResponse.success("Đăng nhập thành công", response);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : adminUsername;

        UserResponse userResponse = UserResponse.builder()
                .username(username)
                .role("ROLE_ADMIN")
                .build();

        return ApiResponse.success(userResponse);
    }
}
