@api @manager @cashier
Feature: Sale cancellation

  Background:
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator
    And I store the first product id and quantity

  Scenario: Cancel completed sale
    When I create a POS sale for stored product with quantity 1
    And I cancel the stored sale with reason "BDD customer returned item"
    Then the response status is 200

  Scenario: Manager can cancel sale
    Given a manager is logged in
    And I store the first product id and quantity
    When I create a POS sale for stored product with quantity 1
    And I cancel the stored sale with reason "Manager void"
    Then the response status is 200
