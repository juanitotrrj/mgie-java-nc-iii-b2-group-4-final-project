package com.group4.inventoryserver.security;

import static org.junit.Assert.*;

import io.jsonwebtoken.Claims;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class JwtUtilTest {

  @Test
  public void generateToken_returnsNonEmptyString() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", 1L);
    claims.put("role", "Administrator");
    String token = JwtUtil.generateToken("1", claims);
    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  public void generateToken_returnsThreePartJwt() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", 1L);
    String token = JwtUtil.generateToken("1", claims);
    String[] parts = token.split("\\.");
    assertEquals(3, parts.length);
  }

  @Test
  public void validateToken_returnsClaimsForValidToken() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", 1L);
    claims.put("role", "Administrator");
    claims.put("username", "admin");
    String token = JwtUtil.generateToken("1", claims);

    Claims parsed = JwtUtil.validateToken(token);
    assertNotNull(parsed);
    assertEquals("1", parsed.getSubject());
    assertEquals(1, ((Number) parsed.get("userId")).intValue());
    assertEquals("Administrator", parsed.get("role"));
    assertEquals("admin", parsed.get("username"));
  }

  @Test
  public void validateToken_returnsNullForGarbageToken() {
    Claims result = JwtUtil.validateToken("not.a.valid.jwt");
    assertNull(result);
  }

  @Test
  public void validateToken_returnsNullForEmptyString() {
    Claims result = JwtUtil.validateToken("");
    assertNull(result);
  }

  @Test
  public void validateToken_returnsNullForTamperedToken() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", 1L);
    String token = JwtUtil.generateToken("1", claims);
    String tampered = token.substring(0, token.length() - 5) + "XXXXX";

    Claims result = JwtUtil.validateToken(tampered);
    assertNull(result);
  }

  @Test
  public void getSubject_returnsSubjectForValidToken() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", 42L);
    String token = JwtUtil.generateToken("42", claims);
    assertEquals("42", JwtUtil.getSubject(token));
  }

  @Test
  public void getSubject_returnsNullForInvalidToken() {
    assertNull(JwtUtil.getSubject("garbage-token"));
  }

  @Test
  public void generateRefreshToken_returnsValidToken() {
    String token = JwtUtil.generateRefreshToken("1");
    assertNotNull(token);
    Claims claims = JwtUtil.validateToken(token);
    assertNotNull(claims);
    assertEquals("1", claims.getSubject());
  }

  @Test
  public void differentSubjects_produceDifferentTokens() {
    Map<String, Object> claims = new HashMap<>();
    String token1 = JwtUtil.generateToken("1", claims);
    String token2 = JwtUtil.generateToken("2", claims);
    assertNotEquals(token1, token2);
  }
}
