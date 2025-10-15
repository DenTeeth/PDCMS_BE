package com.dental.clinic.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class DentalClinicManagementApplication {

	public static void main(String[] args) {
		// Ensure JVM default timezone is a Postgres-accepted value to avoid
		// "invalid value for parameter \"TimeZone\"" during JDBC startup.
		// Use Asia/Ho_Chi_Minh which is equivalent to Vietnam time and valid in Postgres.
		java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

		SpringApplication.run(DentalClinicManagementApplication.class, args);
	}

}
