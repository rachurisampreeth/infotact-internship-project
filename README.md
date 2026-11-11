    # infotact-internship-project

Collaborative project development for the Infotact Solutions internship



## P4 — Docker, Mock Drone Simulator and Testing



### Prerequisites



- Git

- Docker Desktop

- Java 21 JDK



Verify the tools in Git Bash:



```bash

git --version

docker --version

docker compose version

java -version

```



### Start Temporal infrastructure



Run these commands from the repository root.



Start the services:



```bash

docker compose -f infra/docker-compose.yml up -d

```



Check running containers:



```bash

docker compose -f infra/docker-compose.yml ps

```



View Temporal logs:



```bash

docker compose -f infra/docker-compose.yml logs --tail=50 temporal

```



Open the Temporal Web UI at http://localhost:8233.



Stop the infrastructure:



```bash

docker compose -f infra/docker-compose.yml down

```



*\*Note:\*\* The current development configuration uses in-memory persistence. Do not use it as evidence that workflows survive a server restart.



### Run the mock drone simulator



From the repository root, run a 10-drone simulation for 10 seconds:



```bash

java simulator/MockDroneSimulator.java --duration 10

```



Run two drones for six seconds:



```bash

java simulator/MockDroneSimulator.java --drones 2 --duration 6 --interval 1

```



Generate telemetry for 50 drones:



```bash

java simulator/MockDroneSimulator.java --drones 50 --duration 10

```



Display supported arguments:



```bash

java simulator/MockDroneSimulator.java --help

```



By default, telemetry is printed to the terminal. The optional `--send` argument attempts to send telemetry to `http://localhost:8081/api/telemetry`. Use it only when the backend endpoint is available.



### Run the current automated checks



```bash

java simulator/tests/SimulatorChecks.java

```



The current starter checks verify that telemetry is produced for two drones and includes the required field names. Additional contract and integration tests are still required.



### Telemetry contract



Each telemetry record includes `droneId`, `lat`, `lon`, `alt`, `battery`, `step`, `status`, and `timestamp`.



- Latitude and longitude use decimal degrees.

- Altitude uses metres.

- Battery is between 0 and 100.

- Timestamp uses epoch milliseconds.

- Mission steps: `TAKEOFF`, `NAVIGATE`, `DROP`, `RETURN`.

- Statuses: `PENDING`, `ACTIVE`, `COMPLETED`, `FAILED`.



### Known limitations



- The simulator generates mock data only; it does not control real drones.

- Backend integration must be verified against the running Spring Boot endpoint.

- Temporal currently uses in-memory persistence.

- Full 50-drone integration and restart-resilience testing remain to be completed.
## Day 7: Temporal Persistence Test

- Started a test workflow: `persistence-test-001`.
- Restarted the separate `temporal-persistent-test` container.
- Verified that the workflow remained visible with `Running` status after restart.
- Result: Workflow persistence across this test container restart was verified.
- Test Temporal ports: `7234` (gRPC) and `8234` (Web UI).

