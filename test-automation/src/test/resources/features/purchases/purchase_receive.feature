@api @manager @e2e
Feature: Purchase receive

  Background:
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator
    And I store the first supplier id
    And I store the first product id and quantity

  Scenario: Receive pending purchase order
    When I create a purchase order for stored product with quantity 4
    And I receive the stored purchase order
    Then the response status is 200

  Scenario: Cannot receive purchase order twice
    When I create a purchase order for stored product with quantity 2
    And I receive the stored purchase order
    Then the response status is 200
    When I receive the stored purchase order
    Then the response status is 409
