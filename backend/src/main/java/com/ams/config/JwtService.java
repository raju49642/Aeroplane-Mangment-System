package com.ams.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${ams.jwt.secret}") String secret,
            @Value("${ams.jwt.expiration-ms:86400000}") long expirationMillis) {
        // The secret must be long enough for HS256 (>= 256 bits / 32 chars); this is
        // enforced by validating the configured value at startup in application.properties docs.
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(String userName, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setSubject(userName)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token, String expectedUsername) {
        String username = extractUsername(token);
        return username.equals(expectedUsername) && !isTokenExpired(token)
                && extractAllClaims(token).get("purpose") == null;
    }

    public String generatePasswordResetToken(String userName) {
        Date now = new Date();
        return Jwts.builder().setSubject(userName).claim("purpose", "PASSWORD_RESET")
                .setIssuedAt(now).setExpiration(new Date(now.getTime() + 600_000))
                .signWith(signingKey, SignatureAlgorithm.HS256).compact();
    }

    public boolean isPasswordResetTokenValid(String token) {
        Claims claims = extractAllClaims(token);
        return "PASSWORD_RESET".equals(claims.get("purpose", String.class))
                && !claims.getExpiration().before(new Date());
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
