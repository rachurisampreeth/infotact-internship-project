# CONTRACTS.md: AeroSaga team agreements

Source of truth. If you change anything here, announce it in the group and everyone acknowledges.

## 1. Drone telemetry format
```json
{"droneId":"drone-001","lat":17.4,"lon":78.4,"alt":120.0,"battery":85,"step":"NAVIGATE","status":"ACTIVE","timestamp":1760000000000}
```
- lat/lon: decimal degrees (**order is lat, lon**; note Cesium expects lon first)
- alt: metres, battery: 0 to 100, timestamp: epoch milliseconds
- Drone IDs: `drone-001` to `drone-050`
- Ping rate: 1 per second per drone
- JSON fields are camelCase

## 2. Steps and statuses (ALL CAPS)
- Steps: TAKEOFF, NAVIGATE, DROP, RETURN
- Statuses: PENDING, ACTIVE, COMPLETED, FAILED

## 3. WebSocket
- Address: `ws://localhost:8081/ws/telemetry`
- One batch message per second with all drones:
```json
{"type":"telemetry","drones":[ {...}, {...} ]}
```
- Frontend reconnects automatically every 2 seconds if the connection drops
- Backend sends the latest known state when a client connects

## 4. REST API (base: `http://localhost:8081`)
- `POST /api/missions` starts a single mission
- `POST /api/fleets` starts a fleet:
```json
{"fleetId":"fleet-1","drones":[{"droneId":"drone-001","destLat":17.45,"destLon":78.38}]}
```
- `GET /api/missions/{id}/state` returns current step and status per drone
- `POST /api/telemetry` receives pings from mock drones
- Errors: `{"error":"message"}`
- CORS enabled for `http://localhost:5173`

## 5. Temporal
- Server: `localhost:7233`, namespace `default`
- Task queue: `DRONE_MISSION_QUEUE`
- Workflow IDs: `mission-{id}`, `fleet-{id}`
- Workflows: DroneMissionWorkflow, FleetMissionWorkflow
- Activities: takeoff, navigate, dropPackage, returnToBase
- Query: getState

## 6. Ports
| Service | Port |
|---|---|
| Temporal server | 7233 |
| Temporal web UI | [check after docker compose up, e.g. 8233 or 8080] |
| Spring Boot | 8081 |
| React (Vite) | 5173 |

## 7. Versions
- Java 21, Spring Boot 3.x, Maven
- Temporal Java SDK: [version, pinned in pom.xml]
- Node [version], CesiumJS for the globe
- Docker: shared `infra/docker-compose.yml`

## 8. Repo rules
- Folders: `backend/` (P1 owns `workflow` package, P2 owns the rest), `frontend/`, `infra/`
- Branches: [p1-name], [p2-name], [p3-name], [p4-name]. Nobody commits directly to `main`.
- P1 merges to `main` on Days 2, 5, 8, 10. Everyone merges `main` into their own branch each morning.
- Commit messages start with a verb, e.g. "Add fleet workflow"
- Never commit secrets. Use `.env` (git-ignored) and a committed `.env.example`
- A task is done only when it runs, is committed, and whoever depends on it knows

## 9. Mock data
- Default 10 drones in development, 50 for the demo (config setting)
- Location: [city]; depot at [lat, lon]; delivery points 2 to 5 km away
- Mission duration configurable (few minutes in dev, 10 minutes for the resiliency demo)

## 10. Team
| Part | Person | Owns |
|---|---|---|
| P1 | [sampreeth] (lead) | Temporal workflows |
| P2 | [shagun] | Spring Boot, gateway, WebSockets |
| P3 | [aastha] | React and 3D globe |
| P4 | [fazal] | Docker, mock drones, testing, docs |

- Daily check-in: [8 pm] on [WhatsApp]
