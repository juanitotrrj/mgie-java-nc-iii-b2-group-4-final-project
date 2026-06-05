@api @e2e @setup @infra_ready
Feature: Setup wizard journey

  Scenario: Complete setup wizard via API with minted token
    Given the system is in INFRA_READY_APP_SETUP_PENDING state
    And the API server is running
    When I complete the setup wizard via API
    Then the response status is 200
    And I receive a setup session token

  Scenario: After setup finish admin can login
    Given the system is in INFRA_READY_APP_SETUP_PENDING state
    And the API server is running
    When I complete the setup wizard via API
    Then the response status is 200
    When I login as "wizardadmin" with role "Administrator" and password "Wizard@1234"
    Then the response status is 200
    And the login response contains a token
