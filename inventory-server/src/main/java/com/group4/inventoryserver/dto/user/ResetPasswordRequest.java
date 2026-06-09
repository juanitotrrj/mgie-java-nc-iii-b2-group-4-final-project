package com.group4.inventoryserver.dto.user;

public class ResetPasswordRequest {

  private String newPassword;
  private Boolean forceChangeOnNextLogin;

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public Boolean getForceChangeOnNextLogin() {
    return forceChangeOnNextLogin;
  }

  public void setForceChangeOnNextLogin(Boolean forceChangeOnNextLogin) {
    this.forceChangeOnNextLogin = forceChangeOnNextLogin;
  }
}
