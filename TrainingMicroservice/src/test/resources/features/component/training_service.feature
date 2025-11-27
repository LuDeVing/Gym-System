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


Scenario: Process workload - invalid input rejected by service
Given a Training request missing duration
When the workload service processes the request
Then the service should throw BAD_REQUEST