@uat @e2e @manager @clerk
Feature: ICR approval adjusts stock

  Scenario: Approved ICR changes product quantity
    Given the system is initialized
    And the API server is running
    And an inventory clerk is logged in
    And I store the first product id and quantity
    When I submit an inventory adjustment for stored product to quantity 18
    Then the response status is 201
    Given a manager is logged in
    When I approve the stored inventory change request
    Then the response status is 200
    When I get product quantity for stored product
    Then the product quantity equals 18

  Scenario: Stock movement recorded for approved ICR
    Given the system is initialized
    And the API server is running
    And an inventory clerk is logged in
    And I store the first product id and quantity
    When I submit an inventory adjustment for stored product to quantity 20
    Then the response status is 201
    Given a manager is logged in
    When I approve the stored inventory change request
    Then the response status is 200
    When I list stock movements
    Then the response status is 200
    And the response data array is not empty
