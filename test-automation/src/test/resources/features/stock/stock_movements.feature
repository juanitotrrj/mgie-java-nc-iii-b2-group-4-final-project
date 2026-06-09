@api @manager
Feature: Stock movements

  Background:
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator

  Scenario: List stock movements
    When I list stock movements
    Then the response status is 200

  Scenario: Stock movements exist after purchase receive
    And I store the first supplier id
    And I store the first product id and quantity
    And I remember the current product quantity
    When I create a purchase order for stored product with quantity 3
    And I receive the stored purchase order
    Then the response status is 200
    When I list stock movements
    Then the response status is 200
    And the response data array is not empty
