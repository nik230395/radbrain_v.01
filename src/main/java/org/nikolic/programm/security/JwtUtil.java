package org.nikolic.programm.security;

import io.jsonwebtoken. Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.nikolic.programm.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret: mySecretKey123456789012345678901234567890}")
    private String secret;

    @Value("${jwt.expiration:604800000}") // 7 Tage in Millisekunden
    private long jwtExpiration;

    /**
     * Erstellt JWT Token für User
     */
    public String createToken(String email, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System. currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Erstellt JWT Token mit User-Objekt
     */
    public String createToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("fullname", user.getFullname());
        claims.put("role", user.getRole().toString());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm. HS256)
                .compact();
    }

    /**
     * Extrahiert Email aus Token
     */
    public String getEmailFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getSubject();
        } catch (Exception e) {
            logger.error("Error extracting email from token", e);
            return null;
        }
    }

    /**
     * Extrahiert User ID aus Token
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            logger.error("Error extracting userId from token", e);
            return null;
        }
    }

    /**
     * Extrahiert Rolle aus Token
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            logger.error("Error extracting role from token", e);
            return "USER"; // Default fallback
        }
    }

    /**
     * Prüft ob Token gültig ist
     */
    public boolean isValidToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();

            return expiration != null && expiration.after(new Date());
        } catch (Exception e) {
            logger.debug("Token validation failed:  {}", e.getMessage());
            return false;
        }
    }

    /**
     * Prüft ob Token abgelaufen ist
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractAllClaims(token).getExpiration();
            return expiration. before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Extrahiert alle Claims aus Token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Erstellt Signing Key
     */
    private Key getSignInKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Gibt Expiration Time zurück
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }
}