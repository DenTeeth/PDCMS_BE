package com.dental.clinic.management;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
@EnableAsync // Enable async email sending
public class DentalClinicManagementApplication {

	public static void main(String[] args) {
		// Auto-detect environment and set profile
		String profile = detectEnvironment();
		System.setProperty("spring.profiles.active", profile);
		System.out.println("Auto-detected environment: " + profile);

		SpringApplication.run(DentalClinicManagementApplication.class, args);
	}

	/**
	 * Startup logging to verify scheduler configuration
	 */
	@Bean
	public CommandLineRunner schedulerHealthCheck() {
		return args -> {
			ZoneId timezone = ZoneId.of("Asia/Ho_Chi_Minh");
			ZonedDateTime now = ZonedDateTime.now(timezone);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

			log.info("========================================");
			log.info("🚀 PDCMS Backend Application Started");
			log.info("========================================");
			log.info("⏰ Current time: {}", now.format(formatter));
			log.info("🌏 Timezone: {}", timezone);
			log.info("📅 Scheduled jobs are ENABLED");
			log.info("========================================");
			log.info("📋 Active Scheduled Jobs:");
			log.info("  - P8: UnifiedScheduleSyncJob (00:01 AM daily)");
			log.info("  - P9: DailyRenewalDetectionJob (00:05 AM daily)");
			log.info("  - RequestReminderNotificationJob (09:00 AM daily)");
			log.info("  - WarehouseExpiryEmailJob (08:00 AM daily)");
			log.info("  - InactiveEmployeeCleanup (00:00 AM daily)");
			log.info("  - And 6 more cleanup/maintenance jobs...");
			log.info("========================================");
		};
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
