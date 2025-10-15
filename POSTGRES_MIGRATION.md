PostgreSQL migration - docker-compose setup

This file describes how to run PostgreSQL locally with Docker Compose and automatically apply the project's PostgreSQL-compatible SQL initialization scripts.

Files mounted as init scripts
- The compose file mounts `./src/main/resources/db/pg` into the container path `/docker-entrypoint-initdb.d`.
- Any `*.sql` files placed in that directory will be executed once when the database is first initialized (the container data volume is empty).

Quick start (PowerShell)
1) Start PostgreSQL with Docker Compose

```powershell
# From project root
docker compose up -d
```

2) Monitor logs while the DB initializes (optional)

```powershell
docker logs -f dental_clinic_postgres
```

3) Verify tables exist (after init finishes)

```powershell
docker exec -it dental_clinic_postgres psql -U root -d dental_clinic_db -c "\dt"
```

If you need to re-run the init scripts (e.g., after editing SQL files):
- Stop the container and remove the named volume to force re-initialization:

```powershell
docker compose down
# remove the named volume so init scripts run again
docker volume rm dental_postgres_data
# start fresh
docker compose up -d
```

Notes
- The compose file uses credentials `root` / `123456` by design for local development (match your instruction). You can change the environment variables in `docker-compose.yml` or set them via an env file.
- For production do NOT use these credentials; use managed Postgres/secure secrets in CI.
- The SQL files in `src/main/resources/db/pg` were created from the original MySQL scripts and include typical Postgres conversions (DATETIME -> TIMESTAMP, string_agg suggestions, ON CONFLICT where appropriate).

Next steps
- Run the Spring Boot app with environment variables that match the DB (or leave `application.yaml` configured to connect to localhost:5432 with user root/123456).
- If the app uses Flyway or another migration tool, consider registering these scripts with Flyway rather than relying on container init for production migrations.

## Recent changes & troubleshooting

During the local Postgres migration work the project had two small but important changes made to speed up and stabilize developer runs. If you hit startup problems, read this section first.

### JVM timezone fix

- Problem observed: the Postgres driver rejected an invalid timezone string ("Asia/Saigon") during JDBC startup and Hikari pool initialization. This produced a fatal startup error.
- Action taken: the application now sets the JVM default timezone at startup to a Postgres-accepted ID ("Asia/Ho_Chi_Minh") to avoid that handshake failure. This is implemented in `DentalClinicManagementApplication.java` with:

```java
// set at application start (dev convenience)
java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
```

- Recommendation: the cleaner long-term fix is to ensure the container or host provides a valid TZ (see docker-compose TZ note below) or to launch the JVM with `-Duser.timezone=Asia/Ho_Chi_Minh`.

### docker-compose timezone

- We added `TZ: "Asia/Ho_Chi_Minh"` to the Postgres service in `docker-compose.yml` so the DB is initialized with a valid timezone. If you ever re-create the container, keep that `TZ` value.

### Port 8080 conflicts when restarting the app

- Symptom: "Web server failed to start. Port 8080 was already in use." This happens when a previous Java process (your prior run) is still running and listening on port 8080. It is not caused by the Postgres migration changes directly — it is caused by a leftover JVM process.
- Quick diagnostic commands (PowerShell):

```powershell
netstat -ano | findstr ":8080"
tasklist /FI "PID eq <PID>" /V
```

- Quick fix (kill the process by PID):

```powershell
taskkill /PID 4840 /F    # replace 4840 with the PID you saw from netstat
```

- One-liner that finds the LISTENING PID on 8080 and kills it (PowerShell):

```powershell
$p = (netstat -ano | Select-String ":8080" | Select-String "LISTENING" | ForEach-Object { ($_ -split '\\s+')[-1] } | Select-Object -First 1); if ($p) { Write-Host "Killing PID $p"; taskkill /PID $p /F } else { Write-Host "Port 8080 is free" }
```

- I added a helper script `scripts/start-app.ps1` that optionally kills the process listening on 8080 and then starts the app (either via the jar or the same java launch used in dev). Use it like this from project root:

```powershell
# kill and start using the temp classpath launch method used previously
.\scripts\start-app.ps1 -Kill

# or start the built jar directly
.\scripts\start-app.ps1 -Kill -JarPath "target\dental-clinic-management-0.0.1-SNAPSHOT.jar"
```

- Alternatively, configure a different dev port so you can run multiple JVMs without killing anything. Set in `src/main/resources/application.yaml`:

```yaml
server:
	port: 8081
```

- For VS Code users: consider adding a preLaunchTask that runs the kill command before the Java launch so you never need to manually kill the PID.

If you'd like, I can (pick one):

- add the VS Code `launch.json` preLaunchTask that runs the kill task automatically, or
- make `scripts/start-app.ps1` default to -Kill (no prompt), or
- revert the JVM timezone change and instead set an explicit `-Duser.timezone` in your run profile.

Choose which you prefer and I'll apply it.