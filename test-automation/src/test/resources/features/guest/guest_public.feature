@api @uat
Feature: Guest public pages

  Scenario: Welcome page is publicly accessible
    Given the system is initialized
    And the API server is running
    When I GET "/public/welcome"
    Then the response status is 200

  Scenario: About page is publicly accessible
    Given the system is initialized
    And the API server is running
    When I GET "/public/about"
    Then the response status is 200
    And the response body has data field "description"

  Scenario: Contact page is publicly accessible
    Given the system is initialized
    And the API server is running
    When I GET "/public/contact"
    Then the response status is 200
    And the response body has data field "email"

  Scenario: Health endpoint is publicly accessible
    Given the system is initialized
    And the API server is running
    When I GET "/health"
    Then the response status is 200
