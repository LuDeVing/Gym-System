package com.example.org.steps;

import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TrainingStepDefs {

    @Autowired
    private MockMvc mockMvc;

    // store last result for later assertions
    private MvcResult lastResult;

    // helper to create a simple JwtAuthenticationToken (similar to your IntegrationTests helper)
    private JwtAuthenticationToken authToken(String subject, String role) {
        Map<String, Object> headers = Map.of("alg", "none");
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject);
        claims.put("scope", role);
        Instant now = Instant.now();
        Jwt jwt = new Jwt("token-" + UUID.randomUUID(), now, now.plus(1, ChronoUnit.HOURS), headers, claims);
        List<GrantedAuthority> auths = List.of(new SimpleGrantedAuthority(role));
        return new JwtAuthenticationToken(jwt, auths);
    }

    @Given("the system is running")
    public void the_system_is_running() {
        // no-op
    }

    @Given("a trainer {string} exists")
    public void a_trainer_exists(String username) {
        // nothing needed if you stub/import into the Spring context by mocks
    }

    @Given("trainer {string} has a valid JWT")
    public void trainer_has_a_valid_jwt(String username) {
        // store JWT in context if needed - for simplicity we will attach authentication() in the When step
        // Alternatively you could create a field storing the token string.
    }

    @When("the client calls GET /workload/{string}/training-hours")
    public void call_get_training_hours(String username) throws Exception {
        // Attach authentication so security context contains the JwtAuthenticationToken
        lastResult = mockMvc.perform(get("/workload/{username}/training-hours", username)
                        .with(authentication(authToken(username, "ROLE_TRAINER"))))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("the response status should be {int}")
    public void response_status_should_be(int status) throws Exception {
        if (lastResult == null) {
            throw new AssertionError("No request performed yet");
        }
        int actual = lastResult.getResponse().getStatus();
        if (actual != status) {
            throw new AssertionError("Expected status " + status + " but was " + actual);
        }
    }

    @Then("the response contains {string}")
    public void response_contains(String text) throws Exception {
        if (lastResult == null) {
            throw new AssertionError("No request performed yet");
        }
        String body = lastResult.getResponse().getContentAsString();
        if (!body.contains(text)) {
            throw new AssertionError("Response did not contain expected text: " + text + "\nBody was: " + body);
        }
    }
}
