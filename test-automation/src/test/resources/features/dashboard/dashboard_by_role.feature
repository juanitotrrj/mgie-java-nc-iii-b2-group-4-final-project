@api @uat @admin @manager @clerk @cashier
Feature: Dashboard by role

  Background:
    Given the system is initialized
    And the API server is running

  Scenario: Admin dashboard returns metrics
    Given I am logged in as administrator
    When I open the admin dashboard
    Then the response status is 200
    And the response body has data field "cards"

  Scenario: Manager dashboard returns metrics
    Given I am logged in as manager role user
    When I open the manager dashboard
    Then the response status is 200
    And the response body has data field "cards"

  Scenario: Clerk dashboard returns metrics
    Given I am logged in as clerk role user
    When I open the clerk dashboard
    Then the response status is 200
    And the response body has data field "cards"

  Scenario: Cashier dashboard returns metrics
    Given I am logged in as cashier role user
    When I open the cashier dashboard
    Then the response status is 200
    And the response body has data field "cards"
