package com.example.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = {"org.springframework.samples.homepix"})
public class TestConfig {

	TestConfig() {
		System.out.println("Configuring");
	}
}
