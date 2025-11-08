package com.example.org.controllers;

import com.example.org.exceptions.ForbiddenOperationException;
import com.example.org.exceptions.NotFoundException;
import com.example.org.facade.GymFacade;
import com.example.org.model.Trainee;
import com.example.org.model.Trainer;
import com.example.org.requestBodies.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication API", description = "Operations for login and password management")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private GymFacade gymFacade;

    @Autowired
    private JwtEncoder jwtEncoder;

    @PostMapping("/login")
    @Operation(
            summary = "Login with username and password",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Login successful",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid username or password",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    )
            }
    )
    public Map<String, String> login(@RequestBody LoginRequest request) throws ForbiddenOperationException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            Instant now = Instant.now();
            String scope = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(" "));

            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("self")
                    .issuedAt(now)
                    .expiresAt(now.plus(1, ChronoUnit.HOURS))
                    .subject(authentication.getName())
                    .claim("scope", scope)
                    .build();

            String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

            logger.info("User {} logged in successfully, transactionID={}", request.getUsername(), MDC.get("transactionID"));
            return Map.of("token", token, "message", "Login successful");

        } catch (BadCredentialsException e) {
            logger.warn("Invalid login attempt for user {}, transactionID={}", request.getUsername(), MDC.get("transactionID"));
            throw new ForbiddenOperationException("Invalid username or password");
        }
    }

    @PutMapping("/users/{username}/password")
    @Operation(
            summary = "Change password (user must be logged in as themselves)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Password changed successfully",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "User is not allowed to change another user's password",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))
                    )
            }
    )
    public Map<String, String> changePassword(
            @PathVariable String username,
            @RequestParam String newPassword,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) throws ForbiddenOperationException, NotFoundException {

        if (!username.equals(user.getUsername())) {
            logger.warn("User {} tried to change password for {}, transactionID={}", user.getUsername(), username, MDC.get("transactionID"));
            throw new ForbiddenOperationException("You can only change your own password");
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        if(user.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_TRAINEE"))){
            Optional<Trainee> trainee = gymFacade.selectByTraineeName(username);

            if(trainee.isEmpty()){
                logger.warn("Trainee {} not found, transactionID={}", username, MDC.get("transactionID"));
                throw new NotFoundException("Trainee not found");
            }

            trainee.get().setPassword(encodedPassword);
            gymFacade.updateTrainee(trainee.get());

        } else {
            Optional<Trainer> trainer = gymFacade.selectTrainerByUserName(username);

            if(trainer.isEmpty()){
                logger.warn("Trainer {} not found, transactionID={}", username, MDC.get("transactionID"));
                throw new NotFoundException("Trainer not found");
            }

            trainer.get().setPassword(encodedPassword);
            gymFacade.updateTrainer(trainer.get());
        }

        logger.info("Password for user {} changed successfully, transactionID={}", username, MDC.get("transactionID"));
        return Map.of("message", "Password changed successfully");
    }
}
