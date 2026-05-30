package com.group4.inventoryserver.security;

import com.group4.inventoryserver.config.EnvConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JwtUtil {

  private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

  private JwtUtil() {}

  public static String generateToken(String subject, Map<String, Object> claims) {
    long expiryMs = EnvConfig.authTokenExpirySeconds() * 1000L;
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expiryMs);

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public static String generateRefreshToken(String subject) {
    long expiryMs = EnvConfig.authRefreshTokenExpirySeconds() * 1000L;
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expiryMs);

    return Jwts.builder()
        .setSubject(subject)
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public static Claims validateToken(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (ExpiredJwtException e) {
      log.debug("Token expired: {}", e.getMessage());
      return null;
    } catch (MalformedJwtException | SignatureException | IllegalArgumentException e) {
      log.debug("Invalid token: {}", e.getMessage());
      return null;
    }
  }

  public static String getSubject(String token) {
    Claims claims = validateToken(token);
    return claims != null ? claims.getSubject() : null;
  }

  private static Key getSigningKey() {
    byte[] keyBytes = EnvConfig.authTokenSecret().getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
