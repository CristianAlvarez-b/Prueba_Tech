package com.davivienda.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AppApplicationTests {

	@Test
	void contextLoads() {
	}
	@Test
	void testMain_runsWithoutException() {
		String[] args = {};
		// Simplemente llama al main
		AppApplication.main(args);
	}
}
