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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;     // anyString(), eq(), startsWith() etc.

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
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
    public void addTraining_success() throws Exception {
        String traineeUsername = "trainee1";
        String trainerUsername = "trainer1";

        // prepare minimal mocks for trainee and trainer presence
        Trainee mockTrainee = Mockito.mock(Trainee.class);
        when(mockTrainee.getUsername()).thenReturn(traineeUsername);
        when(gymFacade.selectByTraineeName(traineeUsername)).thenReturn(Optional.of(mockTrainee));

        Trainer mockTrainer = Mockito.mock(Trainer.class);
        when(mockTrainer.getUsername()).thenReturn(trainerUsername);
        when(gymFacade.selectTrainerByUserName(trainerUsername)).thenReturn(Optional.of(mockTrainer));

        // no existing training type -> create one
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

        // controller should save the training via the facade
        verify(gymFacade, times(1)).createTraining(any());
    }

    @Test
    public void createTrainee_success_returns_credentials() throws Exception {
        // When creating a trainee the controller calls gymFacade.createTrainee and then updateTrainee
        // We mock returned Trainee to contain username and password
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
}
