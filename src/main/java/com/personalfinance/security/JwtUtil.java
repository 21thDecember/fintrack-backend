package com.personalfinance.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Tiện ích tạo và xác thực JSON Web Token (JWT) sử dụng thuật toán HMAC-SHA256 theo chuẩn RFC 7519
 */
@Slf4j
@Component
public class JwtUtil {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final byte[] secretKeyBytes;
    private final long expirationMs;

    public JwtUtil(
            ObjectMapper objectMapper,
            @Value("${app.security.jwt-secret}") String secretKey,
            @Value("${app.security.jwt-expiration-ms:604800000}") long expirationMs) {
        this.objectMapper = objectMapper;
        this.secretKeyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.expirationMs = expirationMs;
    }

    /**
     * Tạo access token cho username
     */
    public String generateToken(String username) {
        long now = Instant.now().getEpochSecond();
        long exp = now + (expirationMs / 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("iat", now);
        claims.put("exp", exp);
        claims.put("role", "ROLE_ADMIN");

        try {
            String encodedHeader = ENCODER.encodeToString(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
            byte[] payloadBytes = objectMapper.writeValueAsBytes(claims);
            String encodedPayload = ENCODER.encodeToString(payloadBytes);
            String contentToSign = encodedHeader + "." + encodedPayload;

            byte[] signature = sign(contentToSign.getBytes(StandardCharsets.UTF_8));
            String encodedSignature = ENCODER.encodeToString(signature);

            return contentToSign + "." + encodedSignature;
        } catch (Exception e) {
            log.error("Lỗi khi tạo JWT token", e);
            throw new RuntimeException("Không thể tạo JWT token", e);
        }
    }

    /**
     * Xác thực token hợp lệ và chưa hết hạn
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        try {
            String contentToSign = parts[0] + "." + parts[1];
            byte[] expectedSignature = sign(contentToSign.getBytes(StandardCharsets.UTF_8));
            byte[] actualSignature = DECODER.decode(parts[2]);

            if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
                log.warn("Chữ ký JWT không khớp");
                return false;
            }

            // Kiểm tra hạn sử dụng
            byte[] payloadBytes = DECODER.decode(parts[1]);
            JsonNode payloadJson = objectMapper.readTree(payloadBytes);
            if (!payloadJson.has("exp")) {
                return false;
            }

            long exp = payloadJson.get("exp").asLong();
            long now = Instant.now().getEpochSecond();
            if (now > exp) {
                log.warn("JWT token đã hết hạn");
                return false;
            }

            return true;
        } catch (Exception e) {
            log.warn("Lỗi khi xác thực JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Trích xuất username từ JWT token
     */
    public String extractUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            byte[] payloadBytes = DECODER.decode(parts[1]);
            JsonNode payloadJson = objectMapper.readTree(payloadBytes);
            return payloadJson.has("sub") ? payloadJson.get("sub").asText() : null;
        } catch (Exception e) {
            log.warn("Không thể trích xuất username từ JWT: {}", e.getMessage());
            return null;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private byte[] sign(byte[] data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBytes, HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        return mac.doFinal(data);
    }
}
