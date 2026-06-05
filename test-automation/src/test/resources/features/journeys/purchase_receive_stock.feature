@uat @e2e @manager
Feature: Receive purchase increases stock

  Scenario: Manager creates and receives purchase order
    Given the system is initialized
    And the API server is running
    And a manager is logged in
    And I store the first supplier id
    And I store the first product id and quantity
    When I create a purchase order for stored product with quantity 5
    Then the response status is 201
    When I receive the stored purchase order
    Then the response status is 200

  Scenario: Stock quantity increases after receive
    Given the system is initialized
    And the API server is running
    And a manager is logged in
    And I store the first supplier id
    And I store the first product id and quantity
    And I remember the current product quantity
    When I create a purchase order for stored product with quantity 4
    And I receive the stored purchase order
    And I get product quantity for stored product
    Then the stored product quantity increased by 4

  Scenario: Stock movement recorded for purchase receive
    Given the system is initialized
    And the API server is running
    And a manager is logged in
    And I store the first supplier id
    And I store the first product id and quantity
    When I create a purchase order for stored product with quantity 2
    And I receive the stored purchase order
    When I list stock movements
    Then the response status is 200
    And the response data array is not empty
