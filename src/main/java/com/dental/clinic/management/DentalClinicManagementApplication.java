package com.dental.clinic.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DentalClinicManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(DentalClinicManagementApplication.class, args);
	}

}
