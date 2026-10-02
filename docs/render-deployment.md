# Deploy the API to Render

The root `Dockerfile` builds and tests the application using Maven and Java 21, then copies only the executable JAR into a non-root Java 21 runtime. `.dockerignore` excludes local credentials, Git history, documents, and build output. Database migrations still run automatically at application startup against Aiven; no MySQL container or persistent disk is needed.

## Deploy from your repository

1. Commit and push the application, `Dockerfile`, `.dockerignore`, and `render.yaml` to your Git repository. Keep `.env` uncommitted.
2. In Render, choose **New > Blueprint**, connect the repository, and select the branch containing these changes. Render reads `render.yaml` from the repository root.
3. Fill in the prompted secret values:

   | Variable | Value |
   | --- | --- |
   | `DB_PASSWORD` | Your existing Aiven database password |
   | `API_PASSWORD` | Your chosen Super Admin password |
   | `SMTP_PASSWORD` | Your SMTP password or provider app password |

   The API allows browser requests from all origins, including credentialed requests. No CORS environment variable is required.

   Set `API_PASSWORD` in Render Environment for Super Admin login. Password variables use `sync: false`, preserving dashboard-managed values on subsequent Blueprint syncs. Brevo host/login and sender `kbnalpha@gmail.com` are configured in both the Blueprint and Render profile.

   The Blueprint already specifies the Aiven host, port `15129`, database `ehs_db`, user `avnadmin`, and Super Admin username `ehs-api`. SMTP defaults to port `587` with authentication and required STARTTLS; adjust these values to match your provider. Database and API passwords are separate.
4. Review the service plan and create the Blueprint. The configured **Starter plan is paid**. It supports the existing SMTP workflow; [Render Free services block outbound ports 25, 465, and 587](https://render.com/docs/free#other-limitations). Switching to Free requires a supported alternative email transport/provider port before employee activation can work.
5. Wait for the build, Liquibase migrations, and health check to finish. If Aiven uses an IP allowlist, allow your Render service's outbound IP ranges in Aiven.
6. Open `https://<your-service>.onrender.com/actuator/health`. A healthy response is `{"status":"UP"}`. Set `@baseUrl` in `docs/master-api-tests.http` to your Render HTTPS URL and use the configured API credentials. The collection creates test data; run it only against a database where that is intended.

For manual **New > Web Service** setup, choose Docker, repository root as build context, `./Dockerfile` as Dockerfile path, and `/actuator/health` as health-check path. Set the same environment variables listed in `render.yaml`. Leave the Docker command empty so the image entrypoint is used.

## Runtime settings

Set `SPRING_PROFILES_ACTIVE=render` on existing Render services as well as in the Blueprint. The Render profile now contains its own MySQL/Liquibase settings; it does not need `mysql` active and does not import your workstation `.env`.

Before beans or database connections are created, deployment diagnostics log each required setting as `SET (configured or default)` or `MISSING / UNRESOLVED`. DEBUG entries distinguish supplied variables from profile defaults. Values, passwords, SMTP keys, and JDBC URLs are never printed by this diagnostic. Missing or blank settings are reported together in `Render configuration incomplete: ...`. Presence does not prove credentials or connectivity are valid.

For the reported `Could not resolve placeholder 'API_PASSWORD'` failure, set `API_PASSWORD` in Render Environment before redeploying. Also supply `DB_PASSWORD` and `SMTP_PASSWORD`. There is no hardcoded deployment password fallback. Push/redeploy the new code to get these earlier diagnostics. A manually configured Docker service does not apply `render.yaml` automatically.

- `SPRING_PROFILES_ACTIVE=render` enables the standalone Aiven/Render configuration.
- The app binds to `0.0.0.0` and Render's `PORT` (fallback `10000`). HTTPS terminates at Render; forwarded headers are honored.
- TLS is required for Aiven. The dedicated Liquibase session retains the existing primary-key bootstrap configuration. Existing applied migrations are skipped; never clear migration history when redeploying.
- `API_USERNAME` defaults to `ehs-api`. `API_PASSWORD`, `DB_PASSWORD`, and `SMTP_PASSWORD` remain required secrets. For a manually created Docker service, set all three in Render Environment settings; deploying a Dockerfile alone does not apply `render.yaml`. For a Blueprint-managed service, sync the Blueprint. New `sync: false` secrets on an existing service must be added manually.
- CORS allows all origins and credentials on every route, including preflight requests, health/docs routes, and authentication/authorization errors. No frontend-origin configuration is needed.
- `/actuator/health` is public, checks database connectivity, and hides component details. Other management endpoints are not exposed. Business APIs retain authentication and RBAC. SMTP availability is excluded from service health so an email-provider outage does not repeatedly restart the API; activation still reports delivery failures.
- The JVM uses a maximum heap of 65% of container memory. Adjust `JAVA_TOOL_OPTIONS` and/or the service size if workload requires more memory.
- Shutdown is graceful with a 20-second request completion window. Data stays in Aiven across deployments.
- Update secrets in Render's Environment settings and redeploy. `.env` is neither uploaded nor embedded in the image.

## Local Docker verification

Requires Docker with Linux containers. From the repository root:

```powershell
docker build -t ehspro-api:local .
# Set API_USERNAME and API_PASSWORD in this shell before running.
# .env supplies DB settings; add SMTP settings to the environment file for email tests.
docker run --rm --name ehspro-api -p 10000:10000 --env-file .env -e API_USERNAME -e API_PASSWORD ehspro-api:local
```

In another terminal:

```powershell
curl.exe http://localhost:10000/actuator/health
```

This connects to the database specified in `.env` and applies pending migrations. To use another host port, change the left side of `-p`, for example `-p 18080:10000`.

References: [Docker on Render](https://render.com/docs/docker), [Blueprint configuration](https://render.com/docs/blueprint-spec), and [health checks](https://render.com/docs/health-checks).

JWT authentication requires `JWT_SECRET`, a base64-encoded random key of at least 32 bytes. Blueprint sync generates it if absent; manually created Docker services must set it in Environment. `SUPER_ADMIN_EMAIL` defaults to `kbnalpha@gmail.com`.
