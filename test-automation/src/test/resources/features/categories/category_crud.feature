@api @manager @clerk
Feature: Category CRUD

  Background:
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator

  Scenario: List categories
    When I list categories
    Then the response status is 200
    And the response data array is not empty

  Scenario: Create category
    When I create a test category
    Then the response status is 201
    And the stored categoryId is preserved

  Scenario: Get category by id
    When I create a test category
    And I get the stored category by id
    Then the response status is 200

  Scenario: Update category
    When I create a test category
    And I update the stored category description
    Then the response status is 200

  Scenario: Delete category
    When I create a test category
    And I delete the stored category
    Then the response status is 200
