package com.group4.inventoryserver.security;

import static org.junit.Assert.*;

import org.junit.Test;

public class PasswordUtilTest {

  @Test
  public void hash_producesNonNullResult() {
    String hashed = PasswordUtil.hash("Admin@123");
    assertNotNull(hashed);
    assertFalse(hashed.isEmpty());
  }

  @Test
  public void hash_startsWith2aBcryptPrefix() {
    String hashed = PasswordUtil.hash("password");
    assertTrue(hashed.startsWith("$2a$"));
  }

  @Test
  public void hash_producesLengthOf60() {
    String hashed = PasswordUtil.hash("test");
    assertEquals(60, hashed.length());
  }

  @Test
  public void hash_differentCallsProduceDifferentHashes() {
    String hash1 = PasswordUtil.hash("same-password");
    String hash2 = PasswordUtil.hash("same-password");
    assertNotEquals(hash1, hash2);
  }

  @Test
  public void verify_returnsTrueForCorrectPassword() {
    String plaintext = "Admin@123";
    String hashed = PasswordUtil.hash(plaintext);
    assertTrue(PasswordUtil.verify(plaintext, hashed));
  }

  @Test
  public void verify_returnsFalseForWrongPassword() {
    String hashed = PasswordUtil.hash("correct-password");
    assertFalse(PasswordUtil.verify("wrong-password", hashed));
  }

  @Test
  public void verify_worksWithKnownHash() {
    String knownHash = "$2a$10$0qFPf5cpFOjS5zzKDh3rrOHdHiAnPglD83vcuS.cj7AFR7B4aUzDO";
    assertTrue(PasswordUtil.verify("Admin@123", knownHash));
  }

  @Test
  public void meetsPolicy_acceptsStrongPassword() {
    assertTrue(PasswordUtil.meetsPolicy("Admin@123"));
  }

  @Test
  public void meetsPolicy_rejectsNull() {
    assertFalse(PasswordUtil.meetsPolicy(null));
  }

  @Test
  public void meetsPolicy_rejectsShortPassword() {
    assertFalse(PasswordUtil.meetsPolicy("Ab@1"));
  }

  @Test
  public void meetsPolicy_rejectsNoUppercase() {
    assertFalse(PasswordUtil.meetsPolicy("admin@123"));
  }

  @Test
  public void meetsPolicy_rejectsNoLowercase() {
    assertFalse(PasswordUtil.meetsPolicy("ADMIN@123"));
  }

  @Test
  public void meetsPolicy_rejectsNoDigit() {
    assertFalse(PasswordUtil.meetsPolicy("Admin@abc"));
  }

  @Test
  public void meetsPolicy_rejectsNoSymbol() {
    assertFalse(PasswordUtil.meetsPolicy("Admin1234"));
  }
}
