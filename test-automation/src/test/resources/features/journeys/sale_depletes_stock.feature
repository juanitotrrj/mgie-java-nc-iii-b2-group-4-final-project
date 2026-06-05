@uat @e2e @cashier
Feature: Sale depletes stock

  Scenario: Sale reduces product quantity
    Given the system is initialized
    And the API server is running
    And a cashier user exists and is logged in
    And I store the first product id and quantity
    And I remember the current product quantity
    When I create a POS sale for stored product with quantity 1
    Then the response status is 201
    When I get product quantity for stored product
    Then the stored product quantity decreased by 1

  Scenario: Stock movement recorded for sale
    Given the system is initialized
    And the API server is running
    And a cashier user exists and is logged in
    And I store the first product id and quantity
    When I create a POS sale for stored product with quantity 1
    Then the response status is 201
    When I list stock movements
    Then the response status is 200
    And the response data array is not empty
