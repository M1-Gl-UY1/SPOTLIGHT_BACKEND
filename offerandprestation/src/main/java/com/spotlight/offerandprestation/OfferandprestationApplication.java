package com.spotlight.offerandprestation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OfferandprestationApplication {

	public static void main(String[] args) {
		SpringApplication.run(OfferandprestationApplication.class, args);
	}

}
