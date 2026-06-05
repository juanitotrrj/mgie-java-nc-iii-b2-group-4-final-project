@api @e2e @manager
Feature: Product CRUD

  Background:
    Given the system is initialized
    And the API server is running
    And I am logged in as administrator

  Scenario: List products
    When I list products with page 1 and page size 10
    Then the response status is 200
    And the response data array is not empty

  Scenario: Create product
    When I create a test product
    Then the response status is 201
    And the stored productId is preserved

  Scenario: Get product by id
    When I create a test product
    And I get the stored product by id
    Then the response status is 200
    And the response body has data field "productCode"

  Scenario: Update product
    When I create a test product
    And I update the stored product name to "Updated BDD Product"
    Then the response status is 200

  Scenario: Delete product
    When I create a test product
    And I delete the stored product
    Then the response status is 200

  Scenario: Unauthenticated product list is rejected
    When I clear the auth token
    And I list products with page 1 and page size 10
    Then the response status is 401
