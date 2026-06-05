@api @e2e @uat
Feature: User logout

  Scenario: Logout invalidates session
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator
    When I logout via the API
    Then the response status is 200
    When I request the current session profile
    Then the response status is 401

  Scenario: Logout without token returns unauthorized
    Given the system is initialized
    And the API server is running
    When I clear the auth token
    And I POST "/auth/logout" with empty body
    Then the response status is 401
