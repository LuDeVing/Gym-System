package com.example.org;

import com.example.org.controllers.TrainingServiceClient;
import com.example.org.facade.GymFacade;
import com.example.org.model.Trainee;
import com.example.org.model.Trainer;
import com.example.org.model.TrainingType;
import jakarta.jms.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConnectionFactory connectionFactory;

    @MockBean
    private GymFacade gymFacade;

    @MockBean
    private TrainingServiceClient trainingServiceClient;

    private Jwt createJwt(String subject, String scope) {
        Map<String, Object> headers = Map.of("alg", "none");
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject);
        claims.put("scope", scope);
        Instant now = Instant.now();
        return new Jwt("token-" + UUID.randomUUID(), now, now.plus(1, ChronoUnit.HOURS), headers, claims);
    }

    private JwtAuthenticationToken authToken(String subject, String role) {
        Jwt jwt = createJwt(subject, role);
        List<GrantedAuthority> auths = List.of(new SimpleGrantedAuthority(role));
        return new JwtAuthenticationToken(jwt, auths);
    }

    @Test
    public void changePassword_trainee_success() throws Exception {
        String username = "alice";
        String newPassword = "newSecret123";
        Trainee mockTrainee = Mockito.mock(Trainee.class);
        when(mockTrainee.getUsername()).thenReturn(username);
        when(gymFacade.selectByTraineeName(username)).thenReturn(Optional.of(mockTrainee));
        MockHttpServletRequestBuilder req = put("/auth/users/{username}/password", username)
                .param("newPassword", newPassword)
                .with(authentication(authToken(username, "ROLE_TRAINEE")));
        mockMvc.perform(req)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
        verify(gymFacade, times(1)).updateTrainee(mockTrainee);
    }

    @Test
    public void changePassword_forbidden_when_changing_other_user() throws Exception {
        String username = "alice";
        String callingUser = "bob";
        MockHttpServletRequestBuilder req = put("/auth/users/{username}/password", username)
                .param("newPassword", "whatever")
                .with(authentication(authToken(callingUser, "ROLE_TRAINEE")));
        mockMvc.perform(req)
                .andExpect(status().isForbidden());
        verify(gymFacade, never()).updateTrainee(any());
        verify(gymFacade, never()).updateTrainer(any());
    }

    @Test
    public void getTrainerHours_integration_proxy_success() throws Exception {
        String username = "trainer1";
        Map<String, Integer> hours = Map.of("2025-11", 42);
        ResponseEntity<?> resp = ResponseEntity.ok(hours);
        doReturn(resp).when(trainingServiceClient)
                .getHours(argThat(s -> s != null && s.startsWith("Bearer")), anyString(), eq(username));
        MockHttpServletRequestBuilder req = get("/workload/{username}/training-hours", username)
                .with(authentication(authToken(username, "ROLE_TRAINER")));
        mockMvc.perform(req)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['2025-11']").value(42));
        verify(trainingServiceClient, times(1))
                .getHours(argThat(s -> s != null && s.startsWith("Bearer")), anyString(), eq(username));
    }

    @Test
    public void getTrainerHours_integration_proxy_not_found() throws Exception {
        String username = "trainer1";
        doReturn(ResponseEntity.status(404).body(Map.of("error", "not found")))
                .when(trainingServiceClient)
                .getHours(anyString(), anyString(), eq(username));
        mockMvc.perform(get("/workload/{username}/training-hours", username)
                        .with(authentication(authToken(username, "ROLE_TRAINER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Trainer not found in training service"));
        verify(trainingServiceClient, times(1)).getHours(anyString(), anyString(), eq(username));
    }

    @Test
    public void getTrainerHours_integration_proxy_server_error() throws Exception {
        String username = "trainer1";
        doReturn(ResponseEntity.status(500).body(Map.of("error", "internal")))
                .when(trainingServiceClient)
                .getHours(anyString(), anyString(), eq(username));
        mockMvc.perform(get("/workload/{username}/training-hours", username)
                        .with(authentication(authToken(username, "ROLE_TRAINER"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to retrieve training hours"));
        verify(trainingServiceClient, times(1)).getHours(anyString(), anyString(), eq(username));
    }

    @Test
    public void getTrainerHours_microservice_exception() throws Exception {
        String username = "trainer1";
        when(trainingServiceClient.getHours(anyString(), anyString(), eq(username)))
                .thenThrow(new RuntimeException("feign failure"));
        mockMvc.perform(get("/workload/{username}/training-hours", username)
                        .with(authentication(authToken(username, "ROLE_TRAINER"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"));
        verify(trainingServiceClient, times(1)).getHours(anyString(), anyString(), eq(username));
    }

    @Test
    public void addTraining_success() throws Exception {
        String traineeUsername = "trainee1";
        String trainerUsername = "trainer1";
        Trainee mockTrainee = Mockito.mock(Trainee.class);
        when(mockTrainee.getUsername()).thenReturn(traineeUsername);
        when(gymFacade.selectByTraineeName(traineeUsername)).thenReturn(Optional.of(mockTrainee));
        Trainer mockTrainer = Mockito.mock(Trainer.class);
        when(mockTrainer.getUsername()).thenReturn(trainerUsername);
        when(gymFacade.selectTrainerByUserName(trainerUsername)).thenReturn(Optional.of(mockTrainer));
        when(gymFacade.selectTrainingType("Yoga")).thenReturn(Optional.empty());
        TrainingType createdType = Mockito.mock(TrainingType.class);
        when(createdType.getTrainingTypeName()).thenReturn("Yoga");
        when(gymFacade.createTrainingType(any())).thenReturn(createdType);
        String body = "{\n" +
                "  \"traineeUsername\": \"" + traineeUsername + "\",\n" +
                "  \"trainerUsername\": \"" + trainerUsername + "\",\n" +
                "  \"trainingName\": \"Yoga\",\n" +
                "  \"trainingDate\": \"2025-11-01\",\n" +
                "  \"duration\": 60\n" +
                "}";
        MockHttpServletRequestBuilder req = post("/trainings")
                .contentType("application/json")
                .content(body)
                .with(authentication(authToken(trainerUsername, "ROLE_TRAINER")));
        mockMvc.perform(req)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Training added successfully"));
        verify(gymFacade, times(1)).createTraining(any());
    }

    @Test
    public void addTraining_existing_type_success() throws Exception {
        String traineeUsername = "trainee1";
        String trainerUsername = "trainer1";
        Trainee mockTrainee = Mockito.mock(Trainee.class);
        when(mockTrainee.getUsername()).thenReturn(traineeUsername);
        when(gymFacade.selectByTraineeName(traineeUsername)).thenReturn(Optional.of(mockTrainee));
        Trainer mockTrainer = Mockito.mock(Trainer.class);
        when(mockTrainer.getUsername()).thenReturn(trainerUsername);
        when(gymFacade.selectTrainerByUserName(trainerUsername)).thenReturn(Optional.of(mockTrainer));
        TrainingType existingType = Mockito.mock(TrainingType.class);
        when(existingType.getTrainingTypeName()).thenReturn("Yoga");
        when(gymFacade.selectTrainingType("Yoga")).thenReturn(Optional.of(existingType));
        String body = "{\n" +
                "  \"traineeUsername\": \"" + traineeUsername + "\",\n" +
                "  \"trainerUsername\": \"" + trainerUsername + "\",\n" +
                "  \"trainingName\": \"Yoga\",\n" +
                "  \"trainingDate\": \"2025-11-05\",\n" +
                "  \"duration\": 30\n" +
                "}";
        mockMvc.perform(post("/trainings")
                        .contentType("application/json")
                        .content(body)
                        .with(authentication(authToken(trainerUsername, "ROLE_TRAINER"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Training added successfully"));
        verify(gymFacade, times(1)).createTraining(any());
    }

    @Test
    public void addTraining_forbidden_when_trainer_mismatch() throws Exception {
        String trainerUsername = "trainer1";
        String callingUser = "someoneElse";
        String body = "{\n" +
                "  \"traineeUsername\": \"t1\",\n" +
                "  \"trainerUsername\": \"" + trainerUsername + "\",\n" +
                "  \"trainingName\": \"Yoga\",\n" +
                "  \"trainingDate\": \"2025-11-01\",\n" +
                "  \"duration\": 60\n" +
                "}";
        mockMvc.perform(post("/trainings")
                        .contentType("application/json")
                        .content(body)
                        .with(authentication(authToken(callingUser, "ROLE_TRAINER"))))
                .andExpect(status().isForbidden());
        verify(gymFacade, never()).createTraining(any());
    }

    @Test
    public void addTraining_trainee_missing_returns_404() throws Exception {
        String trainerUsername = "trainer1";
        String traineeUsername = "missingTrainee";
        Trainer mockTrainer = Mockito.mock(Trainer.class);
        when(mockTrainer.getUsername()).thenReturn(trainerUsername);
        when(gymFacade.selectTrainerByUserName(trainerUsername)).thenReturn(Optional.of(mockTrainer));
        when(gymFacade.selectByTraineeName(traineeUsername)).thenReturn(Optional.empty());
        String body = "{\n" +
                "  \"traineeUsername\": \"" + traineeUsername + "\",\n" +
                "  \"trainerUsername\": \"" + trainerUsername + "\",\n" +
                "  \"trainingName\": \"Pilates\",\n" +
                "  \"trainingDate\": \"2025-11-02\",\n" +
                "  \"duration\": 45\n" +
                "}";
        mockMvc.perform(post("/trainings")
                        .contentType("application/json")
                        .content(body)
                        .with(authentication(authToken(trainerUsername, "ROLE_TRAINER"))))
                .andExpect(status().isNotFound());
        verify(gymFacade, never()).createTraining(any());
    }

    @Test
    public void createTrainee_success_returns_credentials() throws Exception {
        Trainee created = Mockito.mock(Trainee.class);
        when(created.getUsername()).thenReturn("trainee-abc");
        when(created.getPassword()).thenReturn("raw-pass-xyz");
        when(gymFacade.createTrainee(any(), any(), anyString())).thenReturn(created);
        String body = "{\n" +
                "  \"firstName\": \"John\",\n" +
                "  \"lastName\": \"Doe\",\n" +
                "  \"dateOfBirth\": \"2000-01-01\",\n" +
                "  \"address\": \"123 Street\"\n" +
                "}";
        mockMvc.perform(post("/trainees")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("trainee-abc"))
                .andExpect(jsonPath("$.password").value("raw-pass-xyz"));
        verify(gymFacade, times(1)).updateTrainee(created);
    }

    @Test
    public void getTrainer_not_found_returns_404() throws Exception {
        String trainerUsername = "trainerX";
        when(gymFacade.selectTrainerByUserName(trainerUsername)).thenReturn(Optional.empty());
        mockMvc.perform(get("/trainers/{username}", trainerUsername)
                        .with(authentication(authToken(trainerUsername, "ROLE_TRAINER"))))
                .andExpect(status().isNotFound());
        verify(gymFacade, times(1)).selectTrainerByUserName(trainerUsername);
    }

    @Test
    public void trainer_get_trainings_forbidden_when_other_user() throws Exception {
        String trainerUsername = "trainer1";
        String caller = "notTrainer1";
        mockMvc.perform(get("/trainers/{username}/trainings", trainerUsername)
                        .with(authentication(authToken(caller, "ROLE_TRAINER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    public void trainee_get_not_found_returns_404() throws Exception {
        String username = "unknownTrainee";
        when(gymFacade.selectByTraineeName(username)).thenReturn(Optional.empty());
        mockMvc.perform(get("/trainees/{username}", username)
                        .with(authentication(authToken(username, "ROLE_TRAINEE"))))
                .andExpect(status().isNotFound());
        verify(gymFacade, times(1)).selectByTraineeName(username);
    }

    @Test
    public void trainer_update_success() throws Exception {
        String username = "trainer1";
        Trainer mockTrainer = Mockito.mock(Trainer.class);
        when(mockTrainer.getUsername()).thenReturn(username);
        when(gymFacade.selectTrainerByUserName(username)).thenReturn(Optional.of(mockTrainer));
        String body = "{\n" +
                "  \"firstName\": \"NewFirst\",\n" +
                "  \"lastName\": \"NewLast\",\n" +
                "  \"isActive\": true\n" +
                "}";
        mockMvc.perform(put("/trainers/{username}", username)
                        .contentType("application/json")
                        .content(body)
                        .with(authentication(authToken(username, "ROLE_TRAINER"))))
                .andExpect(status().isOk());
        verify(gymFacade, times(1)).updateTrainer(mockTrainer);
    }

    @Test
    public void trainee_update_success() throws Exception {
        String username = "trainee1";
        Trainee mockTrainee = Mockito.mock(Trainee.class);
        when(mockTrainee.getUsername()).thenReturn(username);
        when(gymFacade.selectByTraineeName(username)).thenReturn(Optional.of(mockTrainee));
        String body = "{\n" +
                "  \"firstName\": \"Updated\",\n" +
                "  \"lastName\": \"User\",\n" +
                "  \"dateOfBirth\": \"1999-01-01\",\n" +
                "  \"address\": \"New Address\",\n" +
                "  \"isActive\": true\n" +
                "}";
        mockMvc.perform(put("/trainees/{username}", username)
                        .contentType("application/json")
                        .content(body)
                        .with(authentication(authToken(username, "ROLE_TRAINEE"))))
                .andExpect(status().isOk());
        verify(gymFacade, times(1)).updateTrainee(mockTrainee);
    }

    @Test
    public void workload_forbidden_when_token_subject_differs() throws Exception {
        String username = "alice";
        String caller = "bob";
        mockMvc.perform(get("/workload/{username}/training-hours", username)
                        .with(authentication(authToken(caller, "ROLE_TRAINER"))))
                .andExpect(status().isForbidden());
    }
}
