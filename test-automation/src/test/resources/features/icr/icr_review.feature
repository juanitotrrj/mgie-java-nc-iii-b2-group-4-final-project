@api @manager @e2e
Feature: Inventory change request review

  Background:
    Given the system is initialized
    And the API server is running
    And an inventory clerk is logged in
    And I store the first product id and quantity

  Scenario: Manager approves pending ICR
    When I submit an inventory adjustment for stored product to quantity 30
    Then the response status is 201
    Given a manager is logged in
    When I approve the stored inventory change request
    Then the response status is 200

  Scenario: Manager rejects pending ICR
    Given an inventory clerk is logged in
    When I submit an inventory adjustment for stored product to quantity 28
    Then the response status is 201
    Given a manager is logged in
    When I reject the stored inventory change request with reason "Insufficient proof"
    Then the response status is 200

  Scenario: Approved ICR adjusts product quantity
    Given an inventory clerk is logged in
    And I store the first product id and quantity
    When I submit an inventory adjustment for stored product to quantity 22
    Then the response status is 201
    Given a manager is logged in
    When I approve the stored inventory change request
    Then the response status is 200
    When I get product quantity for stored product
    Then the product quantity equals 22
