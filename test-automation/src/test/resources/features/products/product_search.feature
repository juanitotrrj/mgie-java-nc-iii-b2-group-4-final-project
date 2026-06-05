@api @manager
Feature: Product search and filters

  Background:
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator

  Scenario: Search products by keyword
    When I search products with keyword "P00"
    Then the response status is 200
    And the response data array is not empty

  Scenario: Filter products by status
    When I filter products by status "In Stock"
    Then the response status is 200
    And the response data array is not empty

  Scenario: Filter products by category
    When I create a test category
    And I create a test product
    And I filter products by category id "categoryId"
    Then the response status is 200
    And the response data array is not empty
