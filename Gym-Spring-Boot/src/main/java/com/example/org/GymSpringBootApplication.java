package com.example.org;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication(scanBasePackages = "com.example.org")
@EnableFeignClients
@EnableDiscoveryClient
public class GymSpringBootApplication {

	public static void main(String[] args) {

		String rawPassword = "yourPlainPassword"; // The password you want to check
		String storedHash = "$2a$10$hnOuz/9lweccH3dV7bzbfOcwGBVOUn2KgMd4illlU2ecQkn7Sor.u"; // DB hash

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		boolean matches = encoder.matches(rawPassword, storedHash);

		System.out.println("Password matches? " + matches);

		SpringApplication.run(GymSpringBootApplication.class, args);
	}

}
