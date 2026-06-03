package com.group4.inventoryserver.dto.setup;

import java.util.List;

public class RoleSeedRequest {

  private List<String> roles;

  public RoleSeedRequest() {}

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }
}
