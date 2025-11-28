Feature: follow feature for training flows

  Background:
    Given the system is running

  Scenario: Trainer retrieves own training hours successfully
    Given a trainer "trainer1" exists
    And trainer "trainer1" has a valid JWT
    And the training microservice will return 200 for "trainer1" with "2025-11" -> 42
    When the client calls GET /workload/trainer1/training-hours
    Then the response status should be 200
    And the response contains "2025-11"

  Scenario: Trainer cannot access another trainer hours
    Given a trainer "alice" exists
    And trainer "bob" has a valid JWT
    When the client calls GET /workload/alice/training-hours
    Then the response status should be 403

  Scenario: Workload microservice returns 404
    Given a trainer "trainer1" exists
    And trainer "trainer1" has a valid JWT
    And the training microservice will return 404 for "trainer1"
    When the client calls GET /workload/trainer1/training-hours
    Then the response status should be 404

  Scenario: Workload microservice returns 500
    Given a trainer "trainer1" exists
    And trainer "trainer1" has a valid JWT
    And the training microservice will return 500 for "trainer1"
    When the client calls GET /workload/trainer1/training-hours
    Then the response status should be 500

  Scenario: Add training success creates type when absent
    Given a trainee "trainee1" exists
    And a trainer "trainer1" exists
    And trainer "trainer1" has a valid JWT
    And training type "Yoga" does not exist
    When the client posts a training for trainee "trainee1" by trainer "trainer1" with name "Yoga" date "2025-11-01" duration 60
    Then the response status should be 201
    And the response contains "Training added successfully"

  Scenario: Add training forbidden if trainer mismatch
    Given a trainee "trainee1" exists
    And a trainer "trainer1" exists
    And "someoneElse" has a valid JWT
    When the client posts a training for trainee "trainee1" by trainer "trainer1" with name "Yoga" date "2025-11-01" duration 60
    Then the response status should be 403

  Scenario: Create trainee returns credentials
    When the client posts a create trainee request with firstName "John" lastName "Doe" dateOfBirth "2000-01-01" address "123 Street"
    Then the response status should be 201
    And the response contains "username"

  Scenario: Change password allowed for self
    Given a trainee "alice" exists
    And "alice" has a valid JWT
    When the client puts /auth/users/alice/password with newPassword "newPass"
    Then the response status should be 200
    And the response contains "Password changed successfully"

  Scenario: Change password forbidden for other user
    Given a trainee "alice" exists
    And "bob" has a valid JWT
    When the client puts /auth/users/alice/password with newPassword "nope"
    Then the response status should be 403

  Scenario: Trainer get not found
    Given a trainer "missingTrainer" does not exist
    And "missingTrainer" has a valid JWT
    When the client calls GET /trainers/missingTrainer
    Then the response status should be 404

  Scenario: Trainee get not found
    Given a trainee "missingT" does not exist
    And "missingT" has a valid JWT
    When the client calls GET /trainees/missingT
    Then the response status should be 404
