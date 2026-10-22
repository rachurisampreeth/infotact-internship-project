# infotact-internship-project

Collaborative project development for the Infotact Solutions internship.

## Day 7: Temporal Persistence Test

- Started a test workflow: `persistence-test-001`.
- Restarted the separate `temporal-persistent-test` container.
- Verified that the workflow remained visible with `Running` status after restart.
- Result: Workflow persistence across this test container restart was verified.
- Test Temporal ports: `7234` (gRPC) and `8234` (Web UI).

## Day 9: Backend API Documentation

### Backend Configuration

- Framework: Spring Boot
- Backend URL: `http://localhost:8081`
- Temporal server: `127.0.0.1:7233`
- Temporal namespace: `default`
- Task queue: `DRONE_MISSION_QUEUE`

### 1. Health Check

- Method: `GET`
- Endpoint: `/api/health`
- Purpose: Checks whether the backend is running.

### 2. Start a Mission

- Method: `POST`
- Endpoint: `/api/missions`
- Purpose: Starts a drone mission using a Temporal workflow.

Request body:

```json
{
  "droneId": "drone-001",
  "destLat": 23.2599,
  "destLon": 78.4126
}
```

Successful response:

```json
{
  "workflowId": "mission-drone-001",
  "status": "STARTED"
}
```

Validation rules:
- `droneId` is required.
- `destLat` must be between -90 and 90.
- `destLon` must be between -180 and 180.

### 3. Get Mission State

- Method: `GET`
- Endpoint: `/api/missions/{droneId}/state`
- Purpose: Retrieves the current mission state.

Example: `GET /api/missions/drone-001/state`

### 4. Submit Drone Telemetry

- Method: `POST`
- Endpoint: `/api/telemetry`
- Purpose: Receives telemetry data from a drone.

Example request:

```json
{
  "droneId": "drone-001",
  "lat": 23.2599,
  "lon": 78.4126,
  "alt": 100,
  "battery": 85,
  "step": "NAVIGATE",
  "status": "ACTIVE",
  "timestamp": 1760000000000
}
```

### 5. Telemetry WebSocket

- URL: `ws://localhost:8081/ws/telemetry`
- Purpose: Streams telemetry updates to connected clients.

Example message:

```json
{
  "type": "telemetry",
  "drones": []
}
```

### 6. Start a Fleet Mission

- Method: `POST`
- Endpoint: `/api/fleets`
- Purpose: Starts missions for multiple drones.

Example request:

```json
{
  "fleetId": "fleet-1",
  "drones": [
    {
      "droneId": "drone-001",
      "destLat": 23.2599,
      "destLon": 78.4126
    }
  ]
}
```

### Frontend Integration

- Frontend URL: `http://localhost:5173`
- Allowed frontend origin: `http://localhost:5173`

### Error Handling

Invalid requests return an HTTP error response containing an error message.

Example:

```json
{
  "error": "destLat must be between -90 and 90"
}
```
