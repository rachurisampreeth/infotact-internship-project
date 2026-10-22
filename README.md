# Infotact Internship Project - AeroSageX Backend

Collaborative project development for the Infotact Solutions internship.

## Backend Overview

- Framework: Spring Boot
- Backend URL: `http://localhost:8081`
- Temporal server: `127.0.0.1:7233`
- Temporal namespace: `default`
- Task queue: `DRONE_MISSION_QUEUE`

## Day-wise Progress

- **Day 1:** Spring Boot backend setup.
- **Day 2:** Temporal workflow integration and mission-start API.
- **Day 3:** Drone telemetry REST API.
- **Day 4:** Telemetry WebSocket endpoint.
- **Day 5:** Fleet mission API and multi-drone workflow execution.
- **Day 6:** Mission state API.
- **Day 7:** Temporal workflow persistence test using a separate test container.
- **Day 8:** Frontend integration testing.
- **Day 9:** Backend API documentation.
- **Day 10:** Final build and regression testing.

## API Documentation

### 1. Health Check

- **Method:** `GET`
- **Endpoint:** `/api/health`
- **Purpose:** Checks whether the backend is running.

### 2. Start a Mission

- **Method:** `POST`
- **Endpoint:** `/api/missions`

Request body:

```json
{
  "droneId": "drone-001",
  "destLat": 23.2599,
  "destLon": 78.4126
}
```

Successful response example:

```json
{
  "workflowId": "mission-drone-001",
  "status": "STARTED"
}
```

Validation:
- `droneId` is required.
- `destLat` must be between -90 and 90.
- `destLon` must be between -180 and 180.

### 3. Get Mission State

- **Method:** `GET`
- **Endpoint:** `/api/missions/{droneId}/state`

Example: `GET /api/missions/drone-001/state`

### 4. Submit Drone Telemetry

- **Method:** `POST`
- **Endpoint:** `/api/telemetry`

Request body:

```json
{
  "droneId": "drone-001",
  "lat": 23.2599,
  "lon": 78.4126,
  "alt": 100,
  "battery": 85,
  "step": "NAVIGATE",
  "status": "ACTIVE",
  "timestamp": 1792705000000
}
```

Validation:
- Latitude must be between -90 and 90.
- Longitude must be between -180 and 180.
- Altitude must be non-negative.
- Battery must be between 0 and 100.
- `step` and `status` must use supported values.
- `timestamp` must be a positive epoch-milliseconds value.

### 5. Telemetry WebSocket

- **URL:** `ws://localhost:8081/ws/telemetry`
- **Purpose:** Streams telemetry updates to connected clients.

Example message:

```json
{
  "type": "telemetry",
  "drones": []
}
```

### 6. Start a Fleet Mission

- **Method:** `POST`
- **Endpoint:** `/api/fleets`
- **Purpose:** Starts missions for multiple drones.

Request body:

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

## Frontend Integration

- Frontend URL: `http://localhost:5173`
- Allowed frontend origin: `http://localhost:5173`

## Testing Summary

- Maven clean package build completed successfully.
- Backend health endpoint returned `UP`.
- Mission state endpoint returned a completed mission state.
- Telemetry API accepted a valid request.
- Fleet workflow and both child drone workflows reached `Completed`.
- Frontend dashboard was tested in a separate local worktree.

## Day 7 Persistence Test

A separate `temporal-persistent-test` container was used to test workflow visibility after restarting that test container. Its ports were `7234` (gRPC) and `8234` (Web UI).

The main backend Temporal server uses port `7233`. These are separate test setups.

## Error Handling

Invalid requests return an HTTP error response containing an error message.

Example:

```json
{
  "error": "destLat must be between -90 and 90"
}
```
