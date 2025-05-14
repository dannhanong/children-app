package com.team.child_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ChildBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChildBeApplication.class, args);
	}

}
