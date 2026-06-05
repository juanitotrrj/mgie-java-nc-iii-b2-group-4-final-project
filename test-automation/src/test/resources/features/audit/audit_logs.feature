@api @admin
Feature: Audit logs

  Background:
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator

  Scenario: Admin lists audit logs
    When I list audit logs
    Then the response status is 200

  Scenario: Cashier cannot view audit logs
    Given a cashier user exists and is logged in
    When I list audit logs
    Then the response status is 403
