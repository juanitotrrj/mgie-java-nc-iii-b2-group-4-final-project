@root-login @deferred
Feature: Root MySQL authentication for setup wizard
  Deferred until POST /api/setup/root-login is implemented in a separate plan.

  Scenario: Administrator authenticates with MySQL root credentials
    Given the system is in INFRA_READY_APP_SETUP_PENDING state
    When I POST root credentials to /setup/root-login
    Then I receive a setup session token
