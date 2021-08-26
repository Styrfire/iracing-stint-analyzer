package com.stintAnalyzer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import javax.inject.Inject;

@SpringBootApplication
public class Application implements CommandLineRunner
{
	@Inject
	StintAnalyzer stintAnalyzer;

	public static void main(String[] args) {
		// can't use just "SpringApplication.run(Application.class, args);" because the console needs for the
		// app to not be headless
		SpringApplicationBuilder builder = new SpringApplicationBuilder(Application.class);
		builder.headless(false);
		builder.run(args);
	}

	@Override
	public void run(String... args) {
		stintAnalyzer.start();
	}
}
