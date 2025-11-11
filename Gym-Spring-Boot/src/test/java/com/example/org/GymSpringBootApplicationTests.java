package com.example.org;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableAutoConfiguration(exclude = {
		org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration.class
})
class GymSpringBootApplicationTests {

	@Test
	void contextLoads() {
	}

}
