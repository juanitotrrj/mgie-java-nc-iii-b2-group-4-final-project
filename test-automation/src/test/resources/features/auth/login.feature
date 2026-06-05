@api @e2e @uat
Feature: User authentication

  Scenario: Admin logs in with valid credentials
    Given the system is initialized
    And the API server is running
    When I login as "admin" with role "Administrator" and password "Admin@123"
    Then the response status is 200
    And the login response contains a token

  Scenario: Login fails with invalid password
    Given the system is initialized
    And the API server is running
    When I login as "admin" with role "Administrator" and password "wrong"
    Then the response status is 401

  Scenario: Login fails with unknown username
    Given the system is initialized
    And the API server is running
    When I login as "unknown-user" with role "Administrator" and password "Admin@123"
    Then the response status is 401

  Scenario: Login fails for deactivated user
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator
    And an inactive user "inactive_bdd" exists with password "Test@1234"
    When I login as "inactive_bdd" with role "Cashier" and password "Test@1234"
    Then the response status is 403
