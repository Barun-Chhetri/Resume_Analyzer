package com.resume.resume_api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class TokenService {

    private static final String HMAC_ALGO = "HmacSHA256";
    private final byte[] secretKeyBytes;

    public TokenService(@Value("${app.security.jwt-secret:resume-intelligence-secure-production-key-2026-auth-session}") String secret) {
        this.secretKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String generateToken(UUID userId) {
        long expiresAtEpochSec = Instant.now().plus(14, ChronoUnit.DAYS).getEpochSecond();
        String payload = userId.toString() + ":" + expiresAtEpochSec;
        String signature = sign(payload);
        return userId.toString() + "." + expiresAtEpochSec + "." + signature;
    }

    public UUID verifyTokenAndGetUserId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        String[] parts = token.trim().split("\\.");
        if (parts.length != 3) {
            return null;
        }

        try {
            String userIdStr = parts[0];
            long expiresAtEpochSec = Long.parseLong(parts[1]);
            String givenSignature = parts[2];

            if (Instant.now().getEpochSecond() > expiresAtEpochSec) {
                return null; // Expired
            }

            String payload = userIdStr + ":" + expiresAtEpochSec;
            String expectedSignature = sign(payload);

            if (!MessageDigest.isEqual(
                    givenSignature.getBytes(StandardCharsets.UTF_8),
                    expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                return null; // Tampered or invalid signature
            }

            return UUID.fromString(userIdStr);
        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(secretKeyBytes, HMAC_ALGO));
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC signature", e);
        }
    }
}
