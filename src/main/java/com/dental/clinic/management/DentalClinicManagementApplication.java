package com.dental.clinic.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
public class DentalClinicManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(DentalClinicManagementApplication.class, args);
	}

}
