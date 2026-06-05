package com.group4.bdd;

public final class SharedTestContext {

  private static final TestContext INSTANCE = new TestContext();

  private SharedTestContext() {}

  public static TestContext get() {
    return INSTANCE;
  }

  public static void reset() {
    INSTANCE.setServerLauncher(null);
    INSTANCE.setHttpClient(null);
    INSTANCE.setSetupToken(null);
    INSTANCE.setLastStatus(0);
    INSTANCE.setLastBody(null);
    INSTANCE.setFixture("initialized");
    INSTANCE.getVars().clear();
  }
}
