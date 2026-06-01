package com.group4.inventoryserver.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class HashUtilTest {

  @Test
  public void sha256_producesConsistentHash() {
    String input = "test-token-value";
    String hash1 = HashUtil.sha256(input);
    String hash2 = HashUtil.sha256(input);
    assertEquals(hash1, hash2);
  }

  @Test
  public void sha256_produces64CharHexString() {
    String hash = HashUtil.sha256("anything");
    assertEquals(64, hash.length());
    assertTrue(hash.matches("^[0-9a-f]+$"));
  }

  @Test
  public void sha256_differentInputsProduceDifferentHashes() {
    String hash1 = HashUtil.sha256("token-a");
    String hash2 = HashUtil.sha256("token-b");
    assertNotEquals(hash1, hash2);
  }

  @Test
  public void sha256_emptyStringHasKnownValue() {
    String hash = HashUtil.sha256("");
    assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", hash);
  }

  @Test
  public void sha256_handlesUnicodeInput() {
    String hash = HashUtil.sha256("contraseña-日本語");
    assertNotNull(hash);
    assertEquals(64, hash.length());
  }
}
