Feature: Training integration flows

  Background:
    Given the system is running

  Scenario: Trainer retrieves own training hours
    Given a trainer "trainer1" exists
    And trainer "trainer1" has a valid JWT
    When the client calls GET /workload/trainer1/training-hours
    Then the response status should be 200
    And the response contains "2025-11"
