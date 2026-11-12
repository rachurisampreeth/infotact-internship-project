# AeroSaga – Drone Mission Management and Simulation Platform

AeroSaga is a collaborative software project developed as part of the Infotact Solutions internship. It focuses on drone mission management, workflow orchestration, real-time telemetry simulation, and monitoring through a web-based dashboard.

The platform combines a Spring Boot backend, a frontend dashboard, Temporal workflow orchestration, and a mock drone simulator to demonstrate automated drone mission execution and monitoring.

## Key Features

- **Drone Mission Management:** Create and manage drone missions through the application.
- **Drone Simulation:** Generate simulated drone telemetry for multiple drones.
- **Workflow Orchestration:** Use Temporal to manage mission execution and workflow states.
- **Real-Time Telemetry:** Track drone location, altitude, battery level, mission progress, and status.
- **Dashboard Monitoring:** Visualize drone activity and mission information through the frontend.
- **Docker Integration:** Run the Temporal infrastructure using Docker Compose.
- **Automated Testing:** Verify simulator output and telemetry structure.
- **Persistence Testing:** Verify workflow availability after restarting a separately configured persistent Temporal test server.

## Technology Stack

| Component | Technology |
|---|---|
| Backend | Java 21, Spring Boot |
| Workflow Orchestration | Temporal |
| Frontend | React.js, JavaScript |
| Infrastructure | Docker, Docker Compose |
| Simulation | Java Mock Drone Simulator |
| Testing | Java Simulator Checks |
| Version Control | Git, GitHub |

## Prerequisites

Install the following tools before running the project:

1. Git
2. Java Development Kit (JDK 21)
3. Docker Desktop
4. Node.js and npm (for the frontend)
5. Maven (if not using the Maven Wrapper included in the project)

Verify the installations using Git Bash or a terminal:

```bash
git --version
java -version
docker --version
docker compose version
node --version
npm --version
```

## Getting Started
### 1. Clone the Repository

Clone the repository using Git:

```bash
git clone https://github.com/rachurisampreeth/infotact-internship-project.git
```

Navigate into the project directory:

```bash
cd infotact-internship-project
```

You can now follow the installation and setup instructions below to run AeroSaga locally.
### 2. Project Structure

The repository contains the following major components:

```text
infotact-internship-project/
│
├── backend/
│   └── Spring Boot application
│
├── frontend/
│   └── React dashboard
│
├── infra/
│   └── Docker Compose configuration
│
├── simulator/
│   ├── MockDroneSimulator.java
│   └── tests/
│       └── SimulatorChecks.java
│
└── README.md
```

The backend, frontend, infrastructure, and simulator are maintained as separate components.

### 3. Start Temporal Infrastructure

AeroSaga uses Temporal for workflow orchestration.

Make sure Docker Desktop is running.

From the repository root, execute:

**Start the services:**

```bash
docker compose -f infra/docker-compose.yml up -d
```

**Check running containers:**

```bash
docker compose -f infra/docker-compose.yml ps
```

**View Temporal server logs:**

```bash
docker compose -f infra/docker-compose.yml logs --tail=50 temporal
```

**Access Temporal Web UI:**

http://localhost:8233

**Stop the infrastructure:**

```bash
docker compose -f infra/docker-compose.yml down
```

**Important:** The standard development configuration currently uses in-memory persistence. Workflows running under this configuration should not be considered restart-resilient.

### 4. Run the Backend

Navigate to the backend directory:

```bash
cd backend
```

Start the Spring Boot application using the available Maven configuration:

```bash
mvn spring-boot:run
```

If the project includes a Maven Wrapper, you can alternatively use:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

Ensure the required backend configuration and Temporal connection settings are available before starting the application.

### 5. Run the Frontend

Open another terminal and navigate to the frontend directory:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

Open the local development URL displayed in the terminal.

The frontend provides the dashboard interface for interacting with and monitoring the drone simulation.

### 6. Run the Mock Drone Simulator

The project includes a Java-based mock drone simulator that generates telemetry data without requiring physical drone hardware.

Run commands from the repository root.

**Default simulation (10 drones for 10 seconds):**

```bash
java simulator/MockDroneSimulator.java --duration 10
```

**Simulate two drones for six seconds:**

```bash
java simulator/MockDroneSimulator.java --drones 2 --duration 6 --interval 1
```

**Simulate 50 drones:**

```bash
java simulator/MockDroneSimulator.java --drones 50 --duration 10
```

**Display supported arguments:**

```bash
java simulator/MockDroneSimulator.java --help
```

By default, generated telemetry is displayed in the terminal.

#### Sending Telemetry to the Backend

The simulator also supports an optional `--send` argument.

Example:

```bash
java simulator/MockDroneSimulator.java --duration 10 --send
```

This attempts to send telemetry to:

```text
POST http://localhost:8081/api/telemetry
```

The backend must be running and expose the corresponding endpoint for this functionality to work.

### 7. Telemetry Data Format

Each simulated drone produces telemetry records containing the following fields:

| Field | Description |
|---|---|
| droneId | Unique identifier of the drone |
| lat | Current latitude in decimal degrees |
| lon | Current longitude in decimal degrees |
| alt | Drone altitude in metres |
| battery | Remaining battery percentage (0–100) |
| step | Current mission execution step |
| status | Current drone mission status |
| timestamp | Timestamp in epoch milliseconds |

**Example telemetry record:**

```json
{
  "droneId": "drone-042",
  "lat": 17.409200,
  "lon": 78.409200,
  "alt": 0.0,
  "battery": 85,
  "step": "NAVIGATE",
  "status": "ACTIVE",
  "timestamp": 1794395825586
}
```

#### Mission Execution Steps

| Step | Description |
|---|---|
| TAKEOFF | Drone begins its mission |
| NAVIGATE | Drone moves towards its destination |
| DROP | Drone performs the delivery operation |
| RETURN | Drone returns to its starting location |

#### Drone Status Values

| Status | Description |
|---|---|
| PENDING | Mission is waiting to start |
| ACTIVE | Mission is currently executing |
| COMPLETED | Mission has successfully finished |
| FAILED | Mission execution has failed |

### 8. Run Automated Tests

The repository includes starter checks for validating mock drone simulator output.

From the repository root, execute:

```bash
java simulator/tests/SimulatorChecks.java
```

The current checks verify:

- Telemetry generation for two drones.
- Presence of the required telemetry fields.
- Basic simulator output structure.

Additional contract, backend integration, and large-scale simulation tests can be added to extend test coverage.

### 9. Temporal Workflow Persistence Testing

A separately configured persistent Temporal test environment is available for checking workflow persistence.

The persistence test verifies whether an existing workflow remains accessible after restarting the Temporal server container.

The test uses:

| Component | Port |
|---|---|
| Temporal gRPC | 7234 |
| Temporal Web UI | 8234 |

The verification procedure is:

1. Start the persistent Temporal test environment.
2. Start a workflow named `persistence-test-001`.
3. Verify that the workflow is running.
4. Restart the separate `temporal-persistent-test` container.
5. Reopen the Temporal Web UI.
6. Check whether the workflow remains visible with its previous `Running` status.

**Result:** Workflow persistence was successfully verified across the tested container restart.

**Note:** This persistence test uses a separate test configuration. It does not mean that the standard development configuration uses persistent storage.

## Important Configuration Notes

Before running the complete application:

- Ensure Docker Desktop is running.
- Ensure the required Java version is installed.
- Start the Temporal infrastructure before running components that depend on it.
- Verify that the backend configuration points to the correct Temporal server.
- Ensure the frontend uses the correct backend URL.
- Confirm that the required backend API endpoints are available.
- Check that the required ports are not occupied by other applications.

## Known Limitations

- The drone simulator generates mock telemetry and does not communicate with or control physical drones.
- Backend telemetry integration requires the corresponding Spring Boot endpoint to be available.
- The standard Temporal development configuration uses in-memory persistence.
- Full 50-drone backend integration testing remains an area for further validation.
- Additional automated contract and integration tests are required.
- Restart resilience has been verified only in the separately configured persistent Temporal test environment.

## Future Improvements

- Integration with real drone telemetry sources.
- Improved real-time dashboard updates.
- Enhanced mission scheduling and tracking.
- Comprehensive backend integration testing.
- Large-scale multi-drone performance testing.
- Persistent workflow storage for production deployments.
- Improved failure recovery and mission retry mechanisms.

## Contributors

This project is collaboratively developed as part of the Infotact Solutions internship.

Contributions, improvements, and suggestions are welcome.

For issues or feature requests, please use the GitHub Issues section of this repository.

---

**AeroSaga – Simulating Smarter Drone Missions Through Workflow Automation.**
