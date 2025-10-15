# Dental Clinic Management (Spring Boot)

A Spring Boot application for managing a private dental clinic.

## Requirements

- Java Development Kit (JDK) 17
- Maven 3.9+ (or use the included Maven Wrapper `mvnw` / `mvnw.cmd`)
-- PostgreSQL 14+ (for local profile)

> Spring Boot 3.x requires Java 17+. This project is configured to compile with Java 17.

## Fixing "JDK isn't specified for module 'dental-clinic-management'"

If your IDE (e.g., IntelliJ IDEA) reports: `JDK isn't specified for module 'dental-clinic-management'`, configure the JDK for the project/module:

- IntelliJ IDEA
  - File → Project Structure → Project → set SDK to "JDK 17"
  - File → Project Structure → Modules → select `dental-clinic-management` → set Language level to 17
  - Also ensure Settings → Build, Execution, Deployment → Build Tools → Maven → JDK for importer = JDK 17

- CLI
  - Make sure `JAVA_HOME` points to a JDK 17 installation
  - On Windows (PowerShell):
    - `$env:JAVA_HOME = 'C:\\Program Files\\Java\\jdk-17'`
    - `setx JAVA_HOME "C:\\Program Files\\Java\\jdk-17"`

## Build & Test

Using Maven Wrapper (recommended):

- Windows:
  - `mvnw.cmd -v`
  - `mvnw.cmd clean test`
- macOS/Linux:
  - `./mvnw -v`
  - `./mvnw clean test`

The `pom.xml` configures the compiler to use Java 17 via:

```xml
<properties>
  <java.version>17</java.version>
  <maven.compiler.source>17</maven.compiler.source>
  <maven.compiler.target>17</maven.compiler.target>
</properties>

<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <configuration>
        <release>17</release>
      </configuration>
    </plugin>
  </plugins>
</build>
```

If the environment still shows the error, it means the machine running the build does not have a JDK 17 configured. Set the JDK as shown above and re-run.

## Run the application

Ensure PostgreSQL is running and the connection info in `src/main/resources/application.yaml` (or `application.properties`) matches your local setup.
- Start the app:
  - Windows: `mvnw.cmd spring-boot:run`
  - macOS/Linux: `./mvnw spring-boot:run`

The app will start on `http://localhost:8080`.

## Notes

- Both `application.yaml` and `application.properties` exist for convenience; you can keep one. YAML is the default in tests.
- Swagger UI is available at `/swagger-ui.html` when the app is running.
