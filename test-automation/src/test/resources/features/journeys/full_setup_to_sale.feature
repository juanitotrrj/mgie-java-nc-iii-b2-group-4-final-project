@uat @e2e
Feature: Full setup to sale journey

  Scenario: Infra ready setup through first sale
    Given the system is in INFRA_READY_APP_SETUP_PENDING state
    And the API server is running
    When I complete the setup wizard via API
    Then the response status is 200
    When I login as "wizardadmin" with role "Administrator" and password "Wizard@1234"
    Then the response status is 200
    And I store the first product id and quantity
    When I create a POS sale for stored product with quantity 1
    Then the response status is 201
    When I get receipt for stored sale
    Then the response status is 200
    And the response body has data field "invoiceNo"

  Scenario: Setup finish enables authenticated product access
    Given the system is in INFRA_READY_APP_SETUP_PENDING state
    And the API server is running
    When I complete the setup wizard via API
    Then the response status is 200
    When I login as "wizardadmin" with role "Administrator" and password "Wizard@1234"
    Then the response status is 200
    When I list products with page 1 and page size 5
    Then the response status is 200
    And the response data array is not empty
