plugins {
	java
    id("org.springframework.boot") version "3.2.10"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.privateclinic"
version = "0.0.1-SNAPSHOT"
description = "Private dental clinic management system with Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // OAuth2 - JWT
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // MySQL
    runtimeOnly("com.mysql:mysql-connector-j")

    // Swagger / OpenAPI (upgrade to avoid NoSuchMethodError with Spring Boot 3.5.x)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}


tasks.withType<Test> {
	useJUnitPlatform()
}
