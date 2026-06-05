@api @cashier @e2e
Feature: POS sale creation

  Background:
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator
    And I store the first product id and quantity

  Scenario: List sales
    When I list sales
    Then the response status is 200

  Scenario: Create POS sale and get receipt
    When I create a POS sale for stored product with quantity 1
    Then the response status is 201
    And the stored saleId is preserved
    When I get receipt for stored sale
    Then the response status is 200
    And the response body has data field "invoiceNo"

  Scenario: Cashier can create sale
    Given a cashier user exists and is logged in
    And I store the first product id and quantity
    When I create a POS sale for stored product with quantity 1
    Then the response status is 201
