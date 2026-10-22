# AeroSageX - 3D Drone Dashboard

A lightweight React + Vite application that visualises a fleet of drones on a **CesiumJS 3-D globe**. The dashboard provides real-time telemetry (simulated or via a backend WebSocket), mission-state lookup, searchable/drill-down drone list, and a set of handy UI controls.

---

## Features

| Feature | Description |
|---------|-------------|
| **3D Globe & Drone Markers** | Cesium globe with 50 simulated drones (or any number supplied by the backend). Each drone has a coloured marker, altitude drop-line and a label showing ID + current step. |
| **Live Telemetry** | Simulated telemetry runs automatically when the backend is unavailable; when a WebSocket connection is established (`ws://localhost:8081/ws/telemetry` by default) live updates replace the simulation. |
| **Drone List Panel** | Search by drone ID, filter by status (ACTIVE / INACTIVE / RETURN), click to fly the camera to a drone. |
| **Telemetry Panel** | Shows latitude, longitude, altitude, speed, battery and current step for the selected drone. |
| **Mission State Integration** | On drone selection the dashboard fetches `/api/missions/{droneId}/state` and displays the mission status badge (uses same colour scheme as drone status). |
| **UI Polish (Day 9)** | Highlighted selected-drone card; high-contrast dark telemetry panel; loading spinner while fetching mission state; concise error message when mission endpoint returns 404 or other errors; keyboard-accessible cards and control buttons (focus outline, ARIA roles); responsive layout for narrow viewports. |
| **Controls** | Zoom-in / zoom-out / reset-camera buttons, plus a help bar describing mouse interactions. |
| **Error Boundary** | Catches render-time errors and displays them in plain text for easier debugging. |

---

## Technologies

| Category | Stack |
|----------|-------|
| **Framework** | React 19 (hooks) |
| **Build / Dev** | Vite 8 |
| **3D Engine** | Cesium 1.146 |
| **Styling** | Plain CSS (no pre-processor) |
| **State / Data** | React `useState`, `useEffect`, custom hooks (`useMissionState`) |
| **Backend Integration** | REST GET for mission state (`fetchMissionState`), WebSocket for live telemetry |
| **Tooling** | ESLint (React hooks & refresh plugins) |

---

## Prerequisites

- **Node.js** >= 20 (LTS)
- **npm** (comes with Node)
- Optional: a running AeroSageX backend (Java) that exposes
  - WebSocket: `ws://localhost:8081/ws/telemetry` (or whatever you set in `VITE_WS_URL`)
  - REST endpoint: `GET /api/missions/{droneId}/state` (CORS configured for `http://localhost:5173`)

If the backend is not available the app will fall back to the built-in telemetry simulation.

---

## Installation

```bash
# clone the repository (if you haven't already)
git clone <repo-url>
cd drone-globe

# install dependencies
npm install
```

### Environment variables (optional)

| Variable | Purpose | Default |
|----------|---------|---------|
| `VITE_WS_URL` | WebSocket URL for live telemetry | `ws://localhost:8081/ws/telemetry` |
| `VITE_API_BASE` | Base URL for the REST API (mission state) | `http://localhost:8081/api` |
| `VITE_CESIUM_ION_TOKEN` | Cesium Ion access token (optional - if omitted the app falls back to OpenStreetMap tiles) | *none* |

Create a `.env` file in the project root if you need to override any of the defaults, for example:

```env
VITE_WS_URL=ws://my-backend:8080/ws/telemetry
VITE_API_BASE=http://my-backend:8080/api
VITE_CESIUM_ION_TOKEN=YOUR_CESIUM_ION_TOKEN
```

*Remember to restart the dev server after changing `.env` files.*

---

## Running the Frontend

```bash
# Development mode (hot-module reload, Vite dev server)
# Default port is 5173; if that port is occupied Vite will automatically use the next free port (e.g., 5174).
npm run dev
```

The app will open automatically in your default browser. If you need a production build:

```bash
npm run build   # creates ./dist
npm run preview # serves the built files locally
```

---

## Backend / Configuration Notes

- **WebSocket** - The client attempts to connect to `VITE_WS_URL`. If the connection fails, the UI falls back to the simulated telemetry loop.
- **Mission-state API** - Calls are made via `fetchMissionState` in `src/api.js`. The helper reads `VITE_API_BASE` (or defaults to `http://localhost:8081/api`). A 404 response is interpreted as "mission not found" and results in a friendly *"Mission state unavailable"* message.
- **CORS** - The backend must allow `http://localhost:5173` (or whichever port the Vite dev server is using) as an origin. The code already sets `credentials: 'omit'`.

If you run the backend on a different host or port, set both `VITE_WS_URL` and `VITE_API_BASE` accordingly.

---

## Current Limitations / Gotchas

- **Mission statuses** are displayed verbatim from the backend response. The UI only knows the three colour mappings defined for drone statuses (`ACTIVE`, `INACTIVE`, `RETURN`). If the backend returns other status values the badge will fall back to a neutral background.
- **No authentication** - The dashboard assumes an open backend (CORS configured for the dev host).
- **Responsive design** - The UI stacks panels on viewports <= 600px; very small screens may still experience overlapping elements due to the fixed-size Cesium canvas.
- **Static assets** - Cesium's static files are served from `/cesium` (Vite's `public` folder). If you move the project, keep the `public/cesium` directory intact.

---

## License

This internship project is for demonstration purposes only. See the repository's `LICENSE` file (if any) for licensing details.

---

*The README now uses only ASCII punctuation and valid UTF-8 encoding.*