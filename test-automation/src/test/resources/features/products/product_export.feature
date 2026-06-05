@api @manager
Feature: Product export

  Background:
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator

  Scenario: Export products as CSV
    When I export products as "csv"
    Then the response status is 200
    And the response body contains text "Product"

  Scenario: Cashier cannot export products
    Given a cashier user exists and is logged in
    When I export products as "csv"
    Then the response status is 403
