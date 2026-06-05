@api @e2e @uat
Feature: Session management

  Scenario: Authenticated user can read session profile
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator
    When I request the current session profile
    Then the response status is 200
    And the session profile username is "admin"

  Scenario: Protected endpoint rejects missing token
    Given the system is initialized
    And the API server is running
    When I clear the auth token
    And I GET "/products?page=1&pageSize=5"
    Then the response status is 401

  Scenario: Protected endpoint rejects invalid token
    Given the system is initialized
    And the API server is running
    When I set an invalid bearer token
    And I GET "/products?page=1&pageSize=5"
    Then the response status is 401
