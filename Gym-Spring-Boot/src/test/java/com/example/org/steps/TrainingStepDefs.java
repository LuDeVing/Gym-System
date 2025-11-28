package com.example.org.steps;

import com.example.org.controllers.TrainingServiceClient;
import com.example.org.facade.GymFacade;
import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TrainingStepDefs {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GymFacade gymFacade;

    @MockBean
    private TrainingServiceClient trainingServiceClient;

    private MvcResult lastResult;

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
    public void system_running() {
    }

    @Given("a trainer {string} exists")
    public void a_trainer_exists(String username) {
    }

    @Given("a trainee {string} exists")
    public void a_trainee_exists(String username) {
    }

    @Given("trainer {string} has a valid JWT")
    public void trainer_has_a_valid_jwt(String username) {
    }

    @Given("{string} has a valid JWT")
    public void user_has_a_valid_jwt(String username) {
    }

    @Given("training type {string} does not exist")
    public void training_type_does_not_exist(String name) {
        when(gymFacade.selectTrainingType(name)).thenReturn(Optional.empty());
    }

    @Given("the training microservice will return 200 for {string} with {string} -> {int}")
    public void microservice_returns_200(String trainer, String yearMonth, Integer hours) {
        Map<String, Integer> out = Map.of(yearMonth, hours);
        when(trainingServiceClient.getHours(argThat(s -> s != null && s.startsWith("Bearer")), anyString(), eq(trainer)))
                .thenReturn(ResponseEntity.ok(out));
    }

    @Given("the training microservice will return 404 for {string}")
    public void microservice_returns_404(String trainer) {
        when(trainingServiceClient.getHours(anyString(), anyString(), eq(trainer)))
                .thenReturn(ResponseEntity.status(404).body(Map.of("error", "not found")));
    }

    @Given("the training microservice will return 500 for {string}")
    public void microservice_returns_500(String trainer) {
        when(trainingServiceClient.getHours(anyString(), anyString(), eq(trainer)))
                .thenReturn(ResponseEntity.status(500).body(Map.of("error", "internal")));
    }

    @When("the client calls GET /workload/{string}/training-hours")
    public void call_get_training_hours(String username) throws Exception {
        lastResult = mockMvc.perform(get("/workload/{username}/training-hours", username)
                        .with(authentication(authToken(username, "ROLE_TRAINER"))))
                .andReturn();
    }

    @When("the client posts a training for trainee {string} by trainer {string} with name {string} date {string} duration {int}")
    public void post_training(String trainee, String trainer, String name, String date, int duration) throws Exception {
        String body = "{\n" +
                "  \"traineeUsername\": \"" + trainee + "\",\n" +
                "  \"trainerUsername\": \"" + trainer + "\",\n" +
                "  \"trainingName\": \"" + name + "\",\n" +
                "  \"trainingDate\": \"" + date + "\",\n" +
                "  \"duration\": " + duration + "\n" +
                "}";
        lastResult = mockMvc.perform(post("/trainings")
                        .contentType("application/json")
                        .content(body)
                        .with(authentication(authToken(trainer, "ROLE_TRAINER"))))
                .andReturn();
    }

    @When("the client posts a create trainee request with firstName {string} lastName {string} dateOfBirth {string} address {string}")
    public void post_create_trainee(String firstName, String lastName, String dateOfBirth, String address) throws Exception {
        String body = "{\n" +
                "  \"firstName\": \"" + firstName + "\",\n" +
                "  \"lastName\": \"" + lastName + "\",\n" +
                "  \"dateOfBirth\": \"" + dateOfBirth + "\",\n" +
                "  \"address\": \"" + address + "\"\n" +
                "}";
        lastResult = mockMvc.perform(post("/trainees")
                        .contentType("application/json")
                        .content(body))
                .andReturn();
    }

    @When("the client puts /auth/users/{string}/password with newPassword {string}")
    public void put_change_password(String username, String newPassword) throws Exception {
        lastResult = mockMvc.perform(put("/auth/users/{username}/password", username)
                        .param("newPassword", newPassword)
                        .with(authentication(authToken(username, "ROLE_TRAINEE"))))
                .andReturn();
    }

    @When("the client puts /auth/users/{string}/password as {string} with newPassword {string}")
    public void put_change_password_as_other(String username, String actor, String newPassword) throws Exception {
        lastResult = mockMvc.perform(put("/auth/users/{username}/password", username)
                        .param("newPassword", newPassword)
                        .with(authentication(authToken(actor, "ROLE_TRAINEE"))))
                .andReturn();
    }

    @When("the client calls GET /trainers/{string}")
    public void call_get_trainer(String username) throws Exception {
        lastResult = mockMvc.perform(get("/trainers/{username}", username)
                        .with(authentication(authToken(username, "ROLE_TRAINER"))))
                .andReturn();
    }

    @When("the client calls GET /trainees/{string}")
    public void call_get_trainee(String username) throws Exception {
        lastResult = mockMvc.perform(get("/trainees/{username}", username)
                        .with(authentication(authToken(username, "ROLE_TRAINEE"))))
                .andReturn();
    }

    @Then("the response status should be {int}")
    public void response_status_should_be(Integer status) throws Exception {
        if (lastResult == null) throw new AssertionError("No request performed");
        int actual = lastResult.getResponse().getStatus();
        if (actual != status) throw new AssertionError("Expected status " + status + " but was " + actual + ". Body: " + lastResult.getResponse().getContentAsString());
    }

    @Then("the response contains {string}")
    public void response_contains(String expected) throws Exception {
        if (lastResult == null) throw new AssertionError("No request performed");
        String body = lastResult.getResponse().getContentAsString();
        if (!body.contains(expected)) throw new AssertionError("Response body did not contain expected text '" + expected + "'. Body: " + body);
    }
}
