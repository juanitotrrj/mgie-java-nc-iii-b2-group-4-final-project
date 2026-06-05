package com.group4.inventoryserver.service;

import com.group4.inventoryserver.config.DatabaseConfig;
import com.group4.inventoryserver.config.EnvConfig;
import com.group4.inventoryserver.dto.setup.BusinessSettingsRequest;
import com.group4.inventoryserver.dto.setup.PermissionSeedRequest;
import com.group4.inventoryserver.dto.setup.RoleSeedRequest;
import com.group4.inventoryserver.dto.setup.SetupFinishResponse;
import com.group4.inventoryserver.dto.setup.SetupProgressResponse;
import com.group4.inventoryserver.dto.setup.SetupStatusResponse;
import com.group4.inventoryserver.dto.setup.SetupUsersRequest;
import com.group4.inventoryserver.repository.SetupSessionRepository;
import com.group4.inventoryserver.repository.SystemInstallationRepository;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetupService {

  private static final Logger log = LoggerFactory.getLogger(SetupService.class);

  private final SystemInstallationRepository installationRepo;
  private final SetupSessionRepository sessionRepo;

  public SetupService() {
    this(new SystemInstallationRepository(), new SetupSessionRepository());
  }

  SetupService(SystemInstallationRepository installationRepo, SetupSessionRepository sessionRepo) {
    this.installationRepo = installationRepo;
    this.sessionRepo = sessionRepo;
  }

  public SetupStatusResponse getStatus() {
    String state = installationRepo.getSetupState();
    String version = installationRepo.getServerVersion();
    boolean dbReady = isDatabaseReady();
    boolean appSetupRequired =
        "INFRA_READY_APP_SETUP_PENDING".equals(state) || "APP_SETUP_IN_PROGRESS".equals(state);
    return new SetupStatusResponse(state, version, appSetupRequired, dbReady);
  }

  public SetupProgressResponse getProgress() {
    String state = installationRepo.getSetupState();
    List<String> completed = new ArrayList<>();
    Map<String, Object> draft = new HashMap<>();

    if ("APP_SETUP_IN_PROGRESS".equals(state) || "INITIALIZED".equals(state)) {
      if (hasSettings()) completed.add("business-settings");
      if (hasRoles()) completed.add("roles");
      if (hasPermissions()) completed.add("permissions");
      if (hasUsers()) completed.add("users");
    }

    SetupProgressResponse response = new SetupProgressResponse();
    response.setCurrentState(state);
    response.setCompletedSteps(completed);
    response.setDraftData(draft);
    return response;
  }

  public void saveBusinessSettings(BusinessSettingsRequest request) {
    transitionToInProgress();
    try (Connection conn = DatabaseConfig.getConnection()) {
      upsertSetting(conn, "company_name", request.getCompanyName());
      upsertSetting(conn, "currency_code", request.getCurrencyCode());
      upsertSetting(conn, "currency_symbol", request.getCurrencySymbol());
      upsertSetting(conn, "currency_name", request.getCurrencyName());
      upsertSetting(conn, "tax_rate", String.valueOf(request.getTaxRate()));
      upsertSetting(conn, "tax_type", request.getTaxType());
      upsertSetting(conn, "price_decimal_places", String.valueOf(request.getPriceDecimalPlaces()));
      upsertSetting(conn, "timezone", request.getTimezone());
    } catch (SQLException e) {
      throw new RuntimeException("Failed to save business settings", e);
    }
  }

  public void seedRoles(RoleSeedRequest request) {
    try (Connection conn = DatabaseConfig.getConnection()) {
      for (String role : request.getRoles()) {
        try (PreparedStatement ps =
            conn.prepareStatement(
                "INSERT IGNORE INTO roles (role_name, description, status) VALUES (?, ?, 'Active')")) {
          ps.setString(1, role);
          ps.setString(2, role + " role");
          ps.executeUpdate();
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to seed roles", e);
    }
  }

  public void seedPermissions(PermissionSeedRequest request) {
    try (Connection conn = DatabaseConfig.getConnection()) {
      for (Map.Entry<String, List<String>> entry : request.getRolePermissions().entrySet()) {
        String roleName = entry.getKey();
        Long roleId = findRoleId(conn, roleName);
        if (roleId == null) {
          log.warn("Role not found for permission seeding: {}", roleName);
          continue;
        }
        for (String permName : entry.getValue()) {
          Long permId = findOrCreatePermission(conn, permName);
          assignPermissionToRole(conn, roleId, permId);
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to seed permissions", e);
    }
  }

  public void createUsers(SetupUsersRequest request) {
    try (Connection conn = DatabaseConfig.getConnection()) {
      for (SetupUsersRequest.SetupUserEntry user : request.getUsers()) {
        Long roleId = findRoleId(conn, user.getRole());
        String hash =
            BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(EnvConfig.passwordBcryptCost()));
        try (PreparedStatement ps =
            conn.prepareStatement(
                "INSERT INTO users (username, password_hash, email, full_name, role_id, status) "
                    + "VALUES (?, ?, ?, ?, ?, 'Active') "
                    + "ON DUPLICATE KEY UPDATE full_name = VALUES(full_name), email = VALUES(email)")) {
          ps.setString(1, user.getUsername());
          ps.setString(2, hash);
          ps.setString(3, user.getEmail());
          ps.setString(4, user.getFullName());
          if (roleId != null) {
            ps.setLong(5, roleId);
          } else {
            ps.setNull(5, java.sql.Types.BIGINT);
          }
          ps.executeUpdate();
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to create users", e);
    }
  }

  public SetupFinishResponse finish() {
    installationRepo.markInitialized();
    sessionRepo.expireAll();
    Timestamp ts = installationRepo.getInitializedAt();
    return new SetupFinishResponse(
        "INITIALIZED", "Setup complete. System is ready.", ts != null ? ts.toString() : null);
  }

  public String createSetupSession() {
    String token = java.util.UUID.randomUUID().toString();
    String hash = sha256(token);
    Timestamp expires = new Timestamp(System.currentTimeMillis() + 3600_000L);
    sessionRepo.create(hash, expires);
    return token;
  }

  public boolean validateSetupToken(String token) {
    if (token == null || token.isEmpty()) return false;
    String hash = sha256(token);
    boolean valid = sessionRepo.isValid(hash);
    if (valid) sessionRepo.touchActivity(hash);
    return valid;
  }

  private void transitionToInProgress() {
    String state = installationRepo.getSetupState();
    if ("INFRA_READY_APP_SETUP_PENDING".equals(state)) {
      installationRepo.updateState("APP_SETUP_IN_PROGRESS");
    }
  }

  private boolean isDatabaseReady() {
    try (Connection conn = DatabaseConfig.getConnection()) {
      return conn.isValid(3);
    } catch (Exception e) {
      return false;
    }
  }

  private boolean hasSettings() {
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "SELECT COUNT(*) FROM settings WHERE setting_key = 'company_name'");
        ResultSet rs = ps.executeQuery()) {
      return rs.next() && rs.getInt(1) > 0;
    } catch (SQLException e) {
      return false;
    }
  }

  private boolean hasRoles() {
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM roles");
        ResultSet rs = ps.executeQuery()) {
      return rs.next() && rs.getInt(1) > 0;
    } catch (SQLException e) {
      return false;
    }
  }

  private boolean hasPermissions() {
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM permissions");
        ResultSet rs = ps.executeQuery()) {
      return rs.next() && rs.getInt(1) > 0;
    } catch (SQLException e) {
      return false;
    }
  }

  private boolean hasUsers() {
    try (Connection conn = DatabaseConfig.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM users");
        ResultSet rs = ps.executeQuery()) {
      return rs.next() && rs.getInt(1) > 0;
    } catch (SQLException e) {
      return false;
    }
  }

  private void upsertSetting(Connection conn, String key, String value) throws SQLException {
    try (PreparedStatement ps =
        conn.prepareStatement(
            "INSERT INTO settings (setting_key, setting_value, category) VALUES (?, ?, 'business') "
                + "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)")) {
      ps.setString(1, key);
      ps.setString(2, value);
      ps.executeUpdate();
    }
  }

  private Long findRoleId(Connection conn, String roleName) throws SQLException {
    try (PreparedStatement ps =
        conn.prepareStatement("SELECT role_id FROM roles WHERE role_name = ?")) {
      ps.setString(1, roleName);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getLong("role_id");
      }
    }
    return null;
  }

  private Long findOrCreatePermission(Connection conn, String permName) throws SQLException {
    try (PreparedStatement ps =
        conn.prepareStatement("SELECT permission_id FROM permissions WHERE permission_name = ?")) {
      ps.setString(1, permName);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) return rs.getLong("permission_id");
      }
    }
    try (PreparedStatement ps =
        conn.prepareStatement(
            "INSERT INTO permissions (permission_name, description) VALUES (?, ?)",
            java.sql.Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, permName);
      ps.setString(2, permName);
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) return keys.getLong(1);
      }
    }
    return null;
  }

  private void assignPermissionToRole(Connection conn, Long roleId, Long permId)
      throws SQLException {
    try (PreparedStatement ps =
        conn.prepareStatement(
            "INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES (?, ?)")) {
      ps.setLong(1, roleId);
      ps.setLong(2, permId);
      ps.executeUpdate();
    }
  }

  private String sha256(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(Charset.forName("UTF-8")));
      StringBuilder hex = new StringBuilder();
      for (byte b : hash) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (Exception e) {
      throw new RuntimeException("SHA-256 not available", e);
    }
  }
}
