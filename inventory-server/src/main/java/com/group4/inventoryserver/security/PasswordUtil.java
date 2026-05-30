package com.group4.inventoryserver.security;

import com.group4.inventoryserver.config.EnvConfig;
import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

  private PasswordUtil() {}

  public static String hash(String plaintext) {
    int cost = EnvConfig.passwordBcryptCost();
    return BCrypt.hashpw(plaintext, BCrypt.gensalt(cost));
  }

  public static boolean verify(String plaintext, String hashed) {
    return BCrypt.checkpw(plaintext, hashed);
  }

  public static boolean meetsPolicy(String password) {
    if (password == null || password.length() < EnvConfig.passwordMinLength()) {
      return false;
    }
    if (EnvConfig.passwordRequireUppercase() && !password.matches(".*[A-Z].*")) {
      return false;
    }
    if (EnvConfig.passwordRequireLowercase() && !password.matches(".*[a-z].*")) {
      return false;
    }
    if (EnvConfig.passwordRequireNumber() && !password.matches(".*\\d.*")) {
      return false;
    }
    if (EnvConfig.passwordRequireSymbol() && !password.matches(".*[^a-zA-Z0-9].*")) {
      return false;
    }
    return true;
  }
}
