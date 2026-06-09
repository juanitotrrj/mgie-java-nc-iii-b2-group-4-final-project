@api @uat
Feature: Guest public inquiry

  Scenario: Guest submits an inquiry
    Given the system is initialized
    And the API server is running
    When I POST a guest inquiry
    Then the response status is 201

  Scenario: Guest inquiry requires message body
    Given the system is initialized
    And the API server is running
    When I POST "/public/inquiries" with JSON:
      """
      {"name":"Guest","email":"guest@test.local","subject":"Hi","message":"BDD test"}
      """
    Then the response status is 201
