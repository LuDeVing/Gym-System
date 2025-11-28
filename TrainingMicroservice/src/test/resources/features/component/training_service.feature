Feature: Training service component tests (controller + service)

  Background:
    Given a component test environment

  Scenario: Get own monthly hours - success
    Given a valid JWT for user "alice"
    And Trainer "alice" has hours for "2025-11"
    When the client requests GET /workload/alice/training-hours with the JWT
    Then the response status should be 200
    And the response body should contain "2025-11"

  Scenario: Get monthly hours - forbidden when token subject differs
    Given a valid JWT for user "bob"
    When the client requests GET /workload/alice/training-hours with the JWT
    Then the service should deny access with 403

  Scenario: Get monthly hours - trainer not found
    Given a valid JWT for user "alice"
    And Trainer "alice" does not exist
    When the client requests GET /workload/alice/training-hours with the JWT
    Then the service should return NOT_FOUND

  Scenario: Process workload - invalid input rejected by service (missing duration)
    Given a Training request missing duration
    When the workload service processes the request
    Then the service should throw BAD_REQUEST

  Scenario: Process workload - missing trainer username rejected
    Given a Training request missing trainer username
    When the workload service processes the request
    Then the service should throw BAD_REQUEST

  Scenario: Process workload - missing training date rejected
    Given a Training request missing training date
    When the workload service processes the request
    Then the service should throw BAD_REQUEST

  Scenario: Process workload - non-positive duration rejected
    Given a Training request with non positive duration
    When the workload service processes the request
    Then the service should throw BAD_REQUEST

  Scenario: Process workload - unknown action rejected
    Given a Training request with unknown action
    When the workload service processes the request
    Then the service should throw BAD_REQUEST

  Scenario: Process workload - delete nonexistent trainer -> 404
    Given store.deleteDuration will throw not found
    When the workload service processes the request
    Then the service should return NOT_FOUND

  Scenario: Process workload - store.addDuration failure -> 400
    Given store.addDuration will throw illegal argument
    When the workload service processes the request
    Then the service should throw BAD_REQUEST
