@api @manager
Feature: Purchase order CRUD

  Background:
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator
    And I store the first supplier id
    And I store the first product id and quantity

  Scenario: List purchase orders
    When I list purchase orders
    Then the response status is 200

  Scenario: Create purchase order
    When I create a purchase order for stored product with quantity 5
    Then the response status is 201
    And the stored purchaseId is preserved

  Scenario: Get purchase order by id
    When I create a purchase order for stored product with quantity 3
    And I get the stored purchase order by id
    Then the response status is 200
    And the response body has data field "purchaseId"

  Scenario: Cancel pending purchase order
    When I create a purchase order for stored product with quantity 2
    And I cancel the stored purchase order with reason "BDD cancel test"
    Then the response status is 200
