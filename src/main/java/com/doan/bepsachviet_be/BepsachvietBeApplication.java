package com.doan.bepsachviet_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BepsachvietBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(BepsachvietBeApplication.class, args);
	}
}
