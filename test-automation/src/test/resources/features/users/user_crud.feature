@api @admin
Feature: User management

  Background:
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator

  Scenario: List users as admin
    When I list users
    Then the response status is 200
    And the response data array is not empty

  Scenario: Create user
    When I create a test cashier user
    Then the response status is 201
    And the stored userId is preserved

  Scenario: Get user by id
    When I create a test cashier user
    And I get the stored user by id
    Then the response status is 200
    And the response body has data field "username"

  Scenario: Update user
    When I create a test cashier user
    And I update the stored user full name to "Updated BDD User"
    Then the response status is 200

  Scenario: Deactivated user cannot login
    When I create a test cashier user
    And I deactivate the stored user
    Then the response status is 200
    When I login as stored username with role "Cashier" and password "Test@1234"
    Then the response status is 403
