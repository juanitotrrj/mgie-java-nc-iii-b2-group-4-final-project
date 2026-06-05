@api @manager @admin
Feature: Report generation

  Background:
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator

  Scenario: Report options returns metadata
    When I get report options
    Then the response status is 200
    And the response body has data field "categories"

  Scenario: Sales summary report
    When I generate the sales summary report
    Then the response status is 200
    And the response body has data field "totalSales"

  Scenario: Inventory value report
    When I generate the inventory value report
    Then the response status is 200
    And the response body has data field "totalInventoryValue"

  Scenario: Low stock report
    When I generate the low stock report
    Then the response status is 200
    And the response body has data field "items"
