package com.coopcredit;

import org.springframework.boot.SpringApplication;

public class TestCoopCreditApplication {

	public static void main(String[] args) {
		SpringApplication.from(CoopCreditApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
