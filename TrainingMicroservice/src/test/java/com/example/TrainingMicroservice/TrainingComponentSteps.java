package com.example.TrainingMicroservice;

import com.example.TrainingMicroservice.controller.Controller;
import com.example.TrainingMicroservice.Service.WorkloadService;
import com.example.TrainingMicroservice.dto.TrainingMicroserviceRequest;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class TrainingComponentSteps {

	private WorkloadService workloadServiceMock;
	private Controller controller;
	private ResponseEntity<?> lastResponse;
	private Exception lastException;
	private Object tempJwt;

	private com.example.TrainingMicroservice.Service.WorkloadService realWorkloadService;
	private com.example.TrainingMicroservice.Stores.TrainerSummaryStore storeMock;
	private TrainingMicroserviceRequest invalidRequest;

	@Given("a component test environment")
	public void setupEnvironment() {
		workloadServiceMock = Mockito.mock(WorkloadService.class);
		controller = new Controller();
		try {
			java.lang.reflect.Field f = Controller.class.getDeclaredField("workloadService");
			f.setAccessible(true);
			f.set(controller, workloadServiceMock);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Given("a valid JWT for user {string}")
	public void valid_jwt_for_user(String username) {
		org.springframework.security.oauth2.jwt.Jwt jwt = new org.springframework.security.oauth2.jwt.Jwt(
				"token",
				Instant.now(),
				Instant.now().plusSeconds(3600),
				Map.of("alg", "none"),
				Map.of("sub", username)
		);
		this.tempJwt = jwt;
	}

	@When("the client requests GET /workload/{string}/training-hours with the JWT")
	public void client_requests_get(String username) {
		try {
			Method target = null;
			for (Method m : controller.getClass().getMethods()) {
				if ("getTrainerHours".equals(m.getName()) && m.getParameterCount() == 3) {
					target = m;
					break;
				}
			}
			if (target == null) throw new NoSuchMethodException("getTrainerHours(String,String,Jwt) not found");
			Object result = target.invoke(controller, username, (String) null, tempJwt);
			lastResponse = (ResponseEntity<?>) result;
			lastException = null;
		} catch (InvocationTargetException ite) {
			Throwable cause = ite.getTargetException();
			if (cause instanceof Exception) lastException = (Exception) cause;
			else lastException = new Exception(cause);
			lastResponse = null;
		} catch (Exception e) {
			lastException = e;
			lastResponse = null;
		}
	}

	@Then("the response status should be {int}")
	public void response_status_should_be(Integer status) {
		Assertions.assertNotNull(lastResponse, "expected a ResponseEntity but got exception: " + lastException);
		Assertions.assertEquals(status.intValue(), lastResponse.getStatusCodeValue());
	}

	@Then("the response body should contain {string}")
	public void response_body_should_contain(String key) {
		Assertions.assertNotNull(lastResponse, "response was null");
		@SuppressWarnings("unchecked")
		Map<String, Integer> body = (Map<String, Integer>) lastResponse.getBody();
		Assertions.assertNotNull(body, "response body was null");
		Assertions.assertTrue(body.containsKey(key), "expected body to contain key: " + key);
	}

	@Then("the service should deny access with 403")
	public void service_should_deny_access() {
		Assertions.assertNotNull(lastException, "expected exception but none thrown");
		Assertions.assertTrue(lastException instanceof ResponseStatusException, "expected ResponseStatusException");
		ResponseStatusException rse = (ResponseStatusException) lastException;
		Assertions.assertEquals(403, rse.getStatusCode().value());
	}

	@Given("a Training request missing duration")
	public void training_request_missing_duration() {
		TrainingMicroserviceRequest req = new TrainingMicroserviceRequest();
		req.setTrainerUsername("alice");
		req.setTrainerFirstName("Alice");
		req.setTrainerLastName("Tester");
		req.setTrainingDate(LocalDate.of(2025, 11, 1));
		req.setTrainingDuration(null); // missing duration
		req.setActionType("ADD");
		this.invalidRequest = req;

		storeMock = Mockito.mock(com.example.TrainingMicroservice.Stores.TrainerSummaryStore.class);
		realWorkloadService = new com.example.TrainingMicroservice.Service.WorkloadService();
		try {
			java.lang.reflect.Field f = com.example.TrainingMicroservice.Service.WorkloadService.class.getDeclaredField("store");
			f.setAccessible(true);
			f.set(realWorkloadService, storeMock);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@When("the workload service processes the request")
	public void workload_service_processes() {
		try {
			realWorkloadService.processWorkload(invalidRequest, null, "token-string");
			lastException = null;
		} catch (Exception e) {
			lastException = e;
		}
	}

	@Then("the service should throw BAD_REQUEST")
	public void service_should_throw_bad_request() {
		Assertions.assertNotNull(lastException, "expected exception but none thrown");
		Assertions.assertTrue(lastException instanceof ResponseStatusException, "expected ResponseStatusException");
		ResponseStatusException rse = (ResponseStatusException) lastException;
		Assertions.assertEquals(400, rse.getStatusCode().value());
	}
}
