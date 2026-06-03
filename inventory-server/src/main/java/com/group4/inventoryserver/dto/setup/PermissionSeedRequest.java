package com.group4.inventoryserver.dto.setup;

import java.util.List;
import java.util.Map;

public class PermissionSeedRequest {

  private Map<String, List<String>> rolePermissions;

  public PermissionSeedRequest() {}

  public Map<String, List<String>> getRolePermissions() {
    return rolePermissions;
  }

  public void setRolePermissions(Map<String, List<String>> rolePermissions) {
    this.rolePermissions = rolePermissions;
  }
}
