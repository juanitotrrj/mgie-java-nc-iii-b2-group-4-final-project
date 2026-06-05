@api @clerk
Feature: Inventory change request submission

  Background:
    Given the system is initialized
    And the API server is running
    And an inventory clerk is logged in
    And I store the first product id and quantity

  Scenario: List inventory change requests
    When I list inventory change requests
    Then the response status is 200

  Scenario: Clerk submits adjustment request
    When I submit an inventory adjustment for stored product to quantity 25
    Then the response status is 201
    And the stored icrId is preserved

  Scenario: Clerk can view personal ICR summary
    When I get my inventory change request summary
    Then the response status is 200
    And the response body has data field "pending"
