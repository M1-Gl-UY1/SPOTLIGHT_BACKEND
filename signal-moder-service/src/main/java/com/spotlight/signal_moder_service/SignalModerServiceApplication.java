package com.spotlight.signal_moder_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class SignalModerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SignalModerServiceApplication.class, args);
	}

}
