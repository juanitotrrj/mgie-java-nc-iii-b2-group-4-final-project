@api @setup @infra_ready
Feature: Server setup API (post-token, no root-login HTTP)

  Scenario: Setup status is readable without token
    Given the API server is running with INFRA_READY fixture
    When I GET "/setup/status"
    Then the response status is 200

  Scenario: Setup status shows INFRA_READY state
    Given the API server is running with INFRA_READY fixture
    When I GET "/setup/status"
    Then the response status is 200
    And the setup state is "INFRA_READY_APP_SETUP_PENDING"

  Scenario: Business settings require setup token
    Given the API server is running with INFRA_READY fixture
    And I have a minted setup token
    When I PUT business settings via setup API
    Then the response status is 200

  Scenario: Business settings without token are rejected
    Given the API server is running with INFRA_READY fixture
    When I PUT business settings without setup token
    Then the response status is 401

  Scenario: Setup progress requires setup token
    Given the API server is running with INFRA_READY fixture
    And I have a minted setup token
    When I GET "/setup/progress"
    Then the response status is 200
    And the response body has data field "completedSteps"

  Scenario: Roles seed accepts setup token
    Given the API server is running with INFRA_READY fixture
    And I have a minted setup token
    When I seed default roles via setup API
    Then the response status is 200

  Scenario: Permissions seed accepts setup token
    Given the API server is running with INFRA_READY fixture
    And I have a minted setup token
    When I seed default permissions via setup API
    Then the response status is 200

  Scenario: Setup users endpoint accepts setup token
    Given the API server is running with INFRA_READY fixture
    And I have a minted setup token
    When I create setup admin user via setup API
    Then the response status is 201
