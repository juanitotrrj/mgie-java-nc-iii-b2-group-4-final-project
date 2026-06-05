@api @manager @clerk
Feature: Supplier CRUD

  Background:
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator

  Scenario: List suppliers
    When I list suppliers
    Then the response status is 200
    And the response data array is not empty

  Scenario: Create supplier
    When I create a test supplier
    Then the response status is 201
    And the stored supplierId is preserved

  Scenario: Get supplier by id
    When I create a test supplier
    And I get the stored supplier by id
    Then the response status is 200

  Scenario: Update supplier
    When I create a test supplier
    And I update the stored supplier phone
    Then the response status is 200

  Scenario: Delete supplier
    When I create a test supplier
    And I delete the stored supplier
    Then the response status is 200
