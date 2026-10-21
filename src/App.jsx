import { useEffect, useRef, useState } from 'react'
import * as Cesium from 'cesium'
import 'cesium/Build/Cesium/Widgets/widgets.css'
import './App.css'

// Ensure Cesium static assets are resolved from /cesium
if (typeof window !== 'undefined') {
  window.CESIUM_BASE_URL = '/cesium'
}

// 5 Initial Drone Waypoints conforming to CONTRACTS.md
// Coordinates order in contracts is lat, lon. Cesium requires lon, lat!
const INITIAL_DRONES = [
  {
    droneId: 'DR-01',
    name: 'Depot Command Drone',
    lat: 17.4000,
    lon: 78.4000,
    alt: 120.0,
    battery: 92,
    speed: 0,
    step: 'NAVIGATE',
    status: 'ACTIVE',
    color: '#00d2ff',
  },
  {
    droneId: 'DR-02',
    name: 'Hitec City Scout',
    lat: 17.4420,
    lon: 78.3800,
    alt: 150.0,
    battery: 85,
    speed: 0,
    step: 'TAKEOFF',
    status: 'ACTIVE',
    color: '#f59e0b',
  },
  {
    droneId: 'DR-03',
    name: 'Charminar Cargo Drone',
    lat: 17.3610,
    lon: 78.4750,
    alt: 105.0,
    battery: 64,
    speed: 0,
    step: 'DROP',
    status: 'ACTIVE',
    color: '#10b981',
  },
  {
    droneId: 'DR-04',
    name: 'Secunderabad Relay Drone',
    lat: 17.4350,
    lon: 78.4980,
    alt: 135.0,
    battery: 48,
    speed: 0,
    step: 'RETURN',
    status: 'ACTIVE',
    color: '#a855f7',
  },
  {
    droneId: 'DR-05',
    name: 'Kukatpally Logistics Drone',
    lat: 17.4900,
    lon: 78.3950,
    alt: 165.0,
    battery: 76,
    speed: 0,
    step: 'NAVIGATE',
    status: 'ACTIVE',
    color: '#38bdf8',
  },
]

// Status colour mapping for telemetry panel
// Status colour mapping for telemetry panel
const STATUS_COLORS = {
  ACTIVE: '#3fb950', // green
  INACTIVE: '#d29922', // orange
  RETURN: '#f85149', // red
  // fallback colour will be used if status not listed
};



const SIMULATION_CONFIG = {
  'DR-01': { centerLat: 17.4000, centerLon: 78.4000, radius: 0.005, speed: 0.06, baseAlt: 120, phase: 0 },
  'DR-02': { centerLat: 17.4420, centerLon: 78.3800, radius: 0.007, speed: 0.05, baseAlt: 150, phase: 1.2 },
  'DR-03': { centerLat: 17.3610, centerLon: 78.4750, radius: 0.006, speed: 0.07, baseAlt: 105, phase: 2.4 },
  'DR-04': { centerLat: 17.4350, centerLon: 78.4980, radius: 0.008, speed: 0.04, baseAlt: 135, phase: 3.6 },
  'DR-05': { centerLat: 17.4900, centerLon: 78.3950, radius: 0.006, speed: 0.05, baseAlt: 165, phase: 4.8 },
}

function App() {
  const containerRef = useRef(null)
  const viewerRef = useRef(null)
  const [drones, setDrones] = useState(INITIAL_DRONES)
  const [selectedDroneId, setSelectedDroneId] = useState(null)
  // search term for drone ID
  const [searchTerm, setSearchTerm] = useState('')
  // status filter (empty means all)
  const [statusFilter, setStatusFilter] = useState('')
  const [connectionMode, setConnectionMode] = useState('SIMULATED') // 'SIMULATED' | 'CONNECTED'
  const simTickRef = useRef(0)

  // Derive filtered list based on search term (case‑insensitive, trimmed) and status filter
  const filteredDrones = drones.filter((d) => {
    const term = searchTerm.trim().toLowerCase();
    const matchesId = term === '' || d.droneId.toLowerCase().includes(term);
    const matchesStatus = statusFilter === '' || d.status === statusFilter;
    return matchesId && matchesStatus;
  });
  const selectedDrone = drones.find((d) => d.droneId === selectedDroneId) || null;

  // 1. In-place update helper for existing Cesium entities (no duplicates created)
  const updateCesiumEntities = (updatedDrones) => {
    if (!viewerRef.current || viewerRef.current.isDestroyed()) return
    const entities = viewerRef.current.entities

    updatedDrones.forEach((drone) => {
      const entity = entities.getById(drone.droneId)
      if (!entity) return

      const pos = Cesium.Cartesian3.fromDegrees(drone.lon, drone.lat, drone.alt)
      const groundPos = Cesium.Cartesian3.fromDegrees(drone.lon, drone.lat, 0)

      // Update position in-place
      entity.position = pos

      // Update label in-place
      if (entity.label) {
        entity.label.text = `${drone.droneId}\n[${drone.step}] • ${Math.round(drone.alt)}m`
      }

      // Update 3D altitude drop-line in-place
      if (entity.polyline) {
        entity.polyline.positions = [groundPos, pos]
      }
    })
  }

  // 2. Initialize Cesium 3D Viewer once
  useEffect(() => {
    if (!containerRef.current) return

    // Configure Cesium Ion token if present
    const ionToken = import.meta.env.VITE_CESIUM_ION_TOKEN
    if (ionToken && ionToken.trim() !== '') {
      Cesium.Ion.defaultAccessToken = ionToken.trim()
    }

    // Fallback imagery: OpenStreetMap if no Ion token is set
    const baseLayer = ionToken && ionToken.trim() !== ''
      ? undefined
      : new Cesium.ImageryLayer(
          new Cesium.OpenStreetMapImageryProvider({
            url: 'https://tile.openstreetmap.org/',
          })
        )

    const viewer = new Cesium.Viewer(containerRef.current, {
      baseLayer: baseLayer,
      animation: false,
      timeline: false,
      geocoder: false,
      homeButton: false,
      sceneModePicker: false,
      baseLayerPicker: false,
      navigationHelpButton: false,
      fullscreenButton: false,
      infoBox: false,
      selectionIndicator: true,
    })

    viewerRef.current = viewer

    // Add initial drone entities (created once with unique ID)
    INITIAL_DRONES.forEach((drone) => {
      const position = Cesium.Cartesian3.fromDegrees(drone.lon, drone.lat, drone.alt)
      const groundPosition = Cesium.Cartesian3.fromDegrees(drone.lon, drone.lat, 0)

      viewer.entities.add({
        id: drone.droneId,
        name: `${drone.droneId} • ${drone.name}`,
        position: position,
        polyline: {
          positions: [groundPosition, position],
          width: 2,
          material: new Cesium.PolylineDashMaterialProperty({
            color: Cesium.Color.fromCssColorString(drone.color).withAlpha(0.6),
            dashLength: 8,
          }),
        },
        point: {
          pixelSize: 16,
          color: Cesium.Color.fromCssColorString(drone.color),
          outlineColor: Cesium.Color.WHITE,
          outlineWidth: 2,
          disableDepthTestDistance: Number.POSITIVE_INFINITY,
        },
        label: {
          text: `${drone.droneId}\n[${drone.step}] • ${drone.alt}m`,
          font: 'bold 12px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
          fillColor: Cesium.Color.WHITE,
          outlineColor: Cesium.Color.BLACK,
          outlineWidth: 3,
          style: Cesium.LabelStyle.FILL_AND_OUTLINE,
          showBackground: true,
          backgroundColor: Cesium.Color.fromCssColorString('rgba(13, 17, 23, 0.8)'),
          backgroundPadding: new Cesium.Cartesian2(7, 4),
          verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
          pixelOffset: new Cesium.Cartesian2(0, -20),
          disableDepthTestDistance: Number.POSITIVE_INFINITY,
        },
      })
    })

    // Click event handler for selecting drones on the 3D globe
    const clickHandler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)
    clickHandler.setInputAction((movement) => {
      const pickedObject = viewer.scene.pick(movement.position)
      if (Cesium.defined(pickedObject) && pickedObject.id) {
        const found = INITIAL_DRONES.find((d) => d.droneId === pickedObject.id.id)
        if (found) {
          setSelectedDroneId(found.droneId)
          return
        }
      }
      setSelectedDroneId(null)
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)

    // Initial camera fly-to over Hyderabad operational hub
    viewer.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(78.4350, 17.4150, 26000),
      orientation: {
        heading: Cesium.Math.toRadians(0),
        pitch: Cesium.Math.toRadians(-45),
        roll: 0.0,
      },
      duration: 1.8,
    })

    return () => {
      if (clickHandler && !clickHandler.isDestroyed()) {
        clickHandler.destroy()
      }
      if (viewer && !viewer.isDestroyed()) {
        viewer.destroy()
      }
      viewerRef.current = null
    }
  }, [])

  // 3. Safe WebSocket integration attempt conforming to CONTRACTS.md (ws://localhost:8081/ws/telemetry)
  useEffect(() => {
    let ws = null
    let isMounted = true
    let reconnectTimer = null

    const initWebSocket = () => {
      try {
        const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8081/ws/telemetry'
        ws = new WebSocket(wsUrl)

        ws.onopen = () => {
          if (!isMounted) return
          setConnectionMode('CONNECTED')
        }

        ws.onmessage = (event) => {
          if (!isMounted) return
          try {
            const data = JSON.parse(event.data)
            if (data.type === 'telemetry' && Array.isArray(data.drones)) {
              setDrones((prevDrones) => {
                const updated = prevDrones.map((drone) => {
                  const incoming = data.drones.find((d) => d.droneId === drone.droneId)
                  if (!incoming) return drone
                  return {
                    ...drone,
                    lat: incoming.lat ?? drone.lat,
                    lon: incoming.lon ?? drone.lon,
                    alt: incoming.alt ?? drone.alt,
                    battery: incoming.battery ?? drone.battery,
                    step: incoming.step ?? drone.step,
                    status: incoming.status ?? drone.status,
                  }
                })
                updateCesiumEntities(updated)
                return updated
              })
            }
          } catch {
            // Ignore parse errors from non-standard test payloads
          }
        }

        ws.onerror = () => {
          if (!isMounted) return
          setConnectionMode('SIMULATED')
        }

        ws.onclose = () => {
          if (!isMounted) return
          setConnectionMode('SIMULATED')
          // Retry connecting in background every 6 seconds
          reconnectTimer = setTimeout(initWebSocket, 6000)
        }
      } catch {
        if (isMounted) {
          setConnectionMode('SIMULATED')
          reconnectTimer = setTimeout(initWebSocket, 6000)
        }
      }
    }

    initWebSocket()

    return () => {
      isMounted = false
      if (reconnectTimer) clearTimeout(reconnectTimer)
      if (ws) {
        ws.onopen = null
        ws.onmessage = null
        ws.onerror = null
        ws.onclose = null
        ws.close()
      }
    }
  }, [])

  // 4. Simulated Telemetry Engine (active when no backend is connected)
  useEffect(() => {
    if (connectionMode === 'CONNECTED') return

    const intervalId = setInterval(() => {
      simTickRef.current += 1
      const tick = simTickRef.current

      setDrones((prevDrones) => {
        const updated = prevDrones.map((drone) => {
          const config = SIMULATION_CONFIG[drone.droneId]
          if (!config) return drone

          // Smooth orbital calculation for natural flight paths
          const angle = tick * config.speed + config.phase
          const lat = config.centerLat + Math.sin(angle) * config.radius
          const lon = config.centerLon + Math.cos(angle) * (config.radius * 1.15)
          const alt = Number((config.baseAlt + Math.sin(angle * 2.2) * 8).toFixed(1))
          const battery = Math.max(12, Number((drone.battery - 0.03).toFixed(1)))

          return {
            ...drone,
            lat: Number(lat.toFixed(5)),
            lon: Number(lon.toFixed(5)),
            alt,
            battery,
            speed: Number((config.speed * 100).toFixed(1)),
          }
        })

        // In-place Cesium entity position/label update
        updateCesiumEntities(updated)
        return updated
      })
    }, 1000) // 1 Hz telemetry refresh rate as agreed in CONTRACTS.md

    return () => {
      clearInterval(intervalId)
    }
  }, [connectionMode])

  // Camera navigation controls
  const handleZoomIn = () => {
    if (!viewerRef.current) return
    const camera = viewerRef.current.camera
    const height = camera.positionCartographic?.height || 10000
    camera.zoomIn(height * 0.35)
  }

  const handleZoomOut = () => {
    if (!viewerRef.current) return
    const camera = viewerRef.current.camera
    const height = camera.positionCartographic?.height || 10000
    camera.zoomOut(height * 0.35)
  }

  const handleResetCamera = () => {
    if (!viewerRef.current) return
    viewerRef.current.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(78.4350, 17.4150, 26000),
      orientation: {
        heading: Cesium.Math.toRadians(0),
        pitch: Cesium.Math.toRadians(-45),
        roll: 0.0,
      },
      duration: 1.2,
    })
  }

  const handleFlyToDrone = (drone) => {
    setSelectedDroneId(drone.droneId)
    if (!viewerRef.current) return
    viewerRef.current.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(drone.lon, drone.lat, 2500),
      orientation: {
        heading: Cesium.Math.toRadians(0),
        pitch: Cesium.Math.toRadians(-35),
        roll: 0.0,
      },
      duration: 1.2,
    })
  }

  return (
    <div className="app-container">
      {/* Cesium 3D Globe Container */}
      <div id="cesiumContainer" ref={containerRef} />

      {/* Floating Mission Control HUD */}
      <div className="mission-hud">
        <div className="mission-hud-title">
          <span>AeroSageX Mission Control</span>
          <span className="mission-hud-badge">Day 4</span>
        </div>
        <div className="mission-hud-sub">
          Automated Telemetry & Live Drone Tracking
        </div>

        {/* Visible Telemetry Source Indicator */}
        <div className="telemetry-source-container">
          {connectionMode === 'CONNECTED' ? (
            <div className="telemetry-source-badge live" title="Active connection to ws://localhost:8081/ws/telemetry">
              <span className="telemetry-dot live-pulse" />
              <span>LIVE WEBSOCKET</span>
            </div>
          ) : (
            <div className="telemetry-source-badge simulated" title="Backend offline: Using built-in realistic telemetry simulation">
              <span className="telemetry-dot simulated-pulse" />
              <span>SIMULATED TELEMETRY</span>
            </div>
          )}
          <div className="telemetry-rate">1 Hz telemetry tick</div>
        </div>

        {/* Search and Filter Controls */}
        <div className="search-filter">
          <input
            type="text"
            placeholder="Search drones (e.g. DR-01)"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="status-select"
          >
            <option value="">All Statuses</option>
            {Array.from(new Set(drones.map((d) => d.status))).map((st) => (
              <option key={st} value={st}>{st}</option>
            ))}
          </select>
        </div>

        {/* Dynamic Drone Card List */}
        <div className="mission-drone-list">
          {filteredDrones.map((drone) => (
            <div
              key={drone.droneId}
              className={`drone-card ${selectedDroneId === drone.droneId ? 'active' : ''}`}
              style={{ borderLeftColor: drone.color }}
              onClick={() => handleFlyToDrone(drone)}
              title="Click to focus camera and view live telemetry"
            >
              <div className="drone-card-info">
                <span className="drone-id">{drone.droneId}</span>
                <span className="drone-battery">{drone.battery}%</span>
              </div>
              <span
                className="drone-step"
                style={{
                  color: drone.color,
                  backgroundColor: `${drone.color}22`,
                }}
              >
                {drone.step}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Telemetry Panel for selected drone */}
      {selectedDrone && (
        <div className="telemetry-panel">
          <h3 className="panel-title">Telemetry – {selectedDrone.droneId}</h3>
          <div className="panel-row"><span className="field-label">Latitude:</span> <span className="field-value">{selectedDrone.lat.toFixed(5)}° N</span></div>
          <div className="panel-row"><span className="field-label">Longitude:</span> <span className="field-value">{selectedDrone.lon.toFixed(5)}° E</span></div>
          <div className="panel-row"><span className="field-label">Altitude:</span> <span className="field-value">{selectedDrone.alt} m</span></div>
          <div className="panel-row"><span className="field-label">Speed:</span> <span className="field-value">{selectedDrone.speed?.toFixed(1) ?? '—'} km/h</span></div>
          <div className="panel-row"><span className="field-label">Battery:</span> <span className="field-value">{selectedDrone.battery}%</span></div>
        </div>
      )}

      {/* Live Updating Drone Information Popup */}
      {selectedDrone && (
        <div className="drone-popup" onClick={(e) => e.stopPropagation()}>
          <div className="drone-popup-header">
            <div className="drone-popup-title-group">
              <span
                className="drone-status-dot"
                style={{ backgroundColor: selectedDrone.color }}
              />
              <h3 className="drone-popup-title">{selectedDrone.droneId}</h3>
            </div>
            <button
              className="drone-popup-close"
              onClick={() => setSelectedDroneId(null)}
              title="Close Popup"
              aria-label="Close"
            >
              ✕
            </button>
          </div>

          <div className="drone-popup-subtitle">
            {selectedDrone.name} • <span style={{ color: connectionMode === 'CONNECTED' ? '#3fb950' : '#d29922' }}>{connectionMode}</span>
          </div>

          <div className="drone-popup-grid">
            <div className="drone-popup-row">
              <span className="field-label">Mission Status:</span>
              <span className="field-value status-badge" style={{
              backgroundColor: STATUS_COLORS[selectedDrone.status] ?? 'rgba(255,255,255,0.1)',
              color: '#f0f6fc',
              padding: '2px 6px',
              borderRadius: '4px',
            }}>{selectedDrone.status}</span>
            </div>
            <div className="drone-popup-row">
              <span className="field-label">Current Step:</span>
              <span
                className="field-value step-badge"
                style={{
                  color: selectedDrone.color,
                  backgroundColor: `${selectedDrone.color}22`,
                }}
              >
                {selectedDrone.step}
              </span>
            </div>
            <div className="drone-popup-row">
              <span className="field-label">Battery Level:</span>
              <div className="battery-container">
                <div className="battery-bar-bg">
                  <div
                    className="battery-bar-fill"
                    style={{
                      width: `${selectedDrone.battery}%`,
                      backgroundColor:
                        selectedDrone.battery > 50
                          ? '#3fb950'
                          : selectedDrone.battery > 25
                          ? '#d29922'
                          : '#f85149',
                    }}
                  />
                </div>
                <span className="battery-text">{selectedDrone.battery}%</span>
              </div>
            </div>
            <div className="drone-popup-row">
              <span className="field-label">Flight Altitude:</span>
              <span className="field-value">{selectedDrone.alt} m</span>
            </div>
            <div className="drone-popup-row">
              <span className="field-label">Live Coordinates:</span>
              <code className="coords-code">
                {selectedDrone.lat.toFixed(5)}° N, {selectedDrone.lon.toFixed(5)}° E
              </code>
            </div>
          </div>
        </div>
      )}

      {/* Navigation & Zoom Control Buttons */}
      <div className="controls-panel">
        <button
          className="control-btn"
          onClick={handleZoomIn}
          title="Zoom In"
          aria-label="Zoom In"
        >
          +
        </button>
        <button
          className="control-btn"
          onClick={handleZoomOut}
          title="Zoom Out"
          aria-label="Zoom Out"
        >
          −
        </button>
        <button
          className="control-btn"
          onClick={handleResetCamera}
          title="Reset Camera to Mission Fleet"
          aria-label="Reset Camera"
        >
          ⌂
        </button>
      </div>

      {/* Camera Gesture Guide */}
      <div className="help-bar">
        <div className="help-item"><span>Left Click:</span> Select Drone</div>
        <div className="help-item"><span>Left Drag:</span> Rotate/Pan</div>
        <div className="help-item"><span>Right Drag / Scroll:</span> Zoom</div>
        <div className="help-item"><span>Middle Drag:</span> Tilt Camera</div>
      </div>
    </div>
  )
}

export default App
