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
		// Auto-detect environment and set profile
		String profile = detectEnvironment();
		System.setProperty("spring.profiles.active", profile);
		System.out.println("Auto-detected environment: " + profile);

		SpringApplication.run(DentalClinicManagementApplication.class, args);
	}

	/**
	 * Auto-detect if running in Docker or local environment
	 * Docker: Uses service names (postgres, redis) → profile = prod
	 * Local: Uses localhost → profile = dev
	 */
	private static String detectEnvironment() {
		// Check if running inside Docker container
		boolean isDocker = System.getenv("DOCKER_CONTAINER") != null ||
				System.getenv("KUBERNETES_SERVICE_HOST") != null ||
				isDockerEnvironment();

		return isDocker ? "prod" : "dev";
	}

	private static boolean isDockerEnvironment() {
		try {
			// Check for .dockerenv file (exists in Docker containers)
			java.io.File dockerEnv = new java.io.File("/.dockerenv");
			if (dockerEnv.exists()) {
				return true;
			}

			// Check cgroup for docker
			java.nio.file.Path cgroup = java.nio.file.Paths.get("/proc/self/cgroup");
			if (java.nio.file.Files.exists(cgroup)) {
				String content = new String(java.nio.file.Files.readAllBytes(cgroup));
				return content.contains("docker") || content.contains("kubepods");
			}
		} catch (Exception ignored) {
		}
		return false;
	}

}
