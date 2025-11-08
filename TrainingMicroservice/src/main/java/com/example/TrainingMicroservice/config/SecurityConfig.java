package com.example.TrainingMicroservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.private-key-location}")
    private org.springframework.core.io.Resource privateKeyResource;

    @Value("${spring.security.oauth2.resourceserver.jwt.public-key-location}")
    private org.springframework.core.io.Resource publicKeyResource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(Customizer.withDefaults())
                );

        return http.build();
    }

    @Bean
    public KeyPair keyPair() throws Exception {
        KeyFactory kf = KeyFactory.getInstance("RSA");

        String privatePem = new String(privateKeyResource.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        String publicPem  = new String(publicKeyResource.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

        // Strip PEM markers and ALL whitespace (including Windows CRLF)
        privatePem = privatePem.replaceAll("-----BEGIN (?:.*)-----", "")
                .replaceAll("-----END (?:.*)-----", "")
                .replaceAll("\\s", "");
        publicPem  = publicPem.replaceAll("-----BEGIN (?:.*)-----", "")
                .replaceAll("-----END (?:.*)-----", "")
                .replaceAll("\\s", "");

        byte[] privBytes = java.util.Base64.getDecoder().decode(privatePem);
        byte[] pubBytes  = java.util.Base64.getDecoder().decode(publicPem);

        PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes));
        PublicKey publicKey   = kf.generatePublic(new X509EncodedKeySpec(pubBytes));

        return new KeyPair(publicKey, privateKey);
    }


    @Bean
    public JwtDecoder jwtDecoder(KeyPair keyPair) {
        return NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
    }

}