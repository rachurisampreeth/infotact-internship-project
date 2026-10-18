import { useEffect, useRef, useState } from 'react'
import * as Cesium from 'cesium'
import 'cesium/Build/Cesium/Widgets/widgets.css'
import './App.css'

// Ensure Cesium static assets are resolved from /cesium
if (typeof window !== 'undefined') {
  window.CESIUM_BASE_URL = '/cesium'
}

// 5 Static mock drones conforming to CONTRACTS.md
// Coordinates order in contracts is lat, lon. Cesium requires lon, lat!
const STATIC_DRONES = [
  {
    droneId: 'DR-01',
    name: 'Depot Command Drone',
    lat: 17.4000,
    lon: 78.4000,
    alt: 120.0,
    battery: 92,
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
    step: 'NAVIGATE',
    status: 'ACTIVE',
    color: '#38bdf8',
  },
]

function App() {
  const containerRef = useRef(null)
  const viewerRef = useRef(null)
  const [selectedDrone, setSelectedDrone] = useState(null)

  useEffect(() => {
    if (!containerRef.current) return

    // 1. Configure Cesium Ion token if present in environment
    const ionToken = import.meta.env.VITE_CESIUM_ION_TOKEN
    if (ionToken && ionToken.trim() !== '') {
      Cesium.Ion.defaultAccessToken = ionToken.trim()
    }

    // 2. Fallback imagery provider: OpenStreetMap if no Ion token is set
    const baseLayer = ionToken && ionToken.trim() !== ''
      ? undefined
      : new Cesium.ImageryLayer(
          new Cesium.OpenStreetMapImageryProvider({
            url: 'https://tile.openstreetmap.org/',
          })
        )

    // 3. Initialize Cesium 3D Globe Viewer
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
      infoBox: false, // We use custom React popup state for reliable, unblocked rendering
      selectionIndicator: true,
    })

    viewerRef.current = viewer

    // 4. Add 5 static drone markers with clear styling and labels
    STATIC_DRONES.forEach((drone) => {
      // NOTE: Cesium expects lon first: fromDegrees(lon, lat, alt)
      const position = Cesium.Cartesian3.fromDegrees(
        drone.lon,
        drone.lat,
        drone.alt
      )
      const groundPosition = Cesium.Cartesian3.fromDegrees(
        drone.lon,
        drone.lat,
        0
      )

      viewer.entities.add({
        id: drone.droneId,
        name: `${drone.droneId} • ${drone.name}`,
        position: position,
        // Altitude indicator line from ground to drone position
        polyline: {
          positions: [groundPosition, position],
          width: 2,
          material: new Cesium.PolylineDashMaterialProperty({
            color: Cesium.Color.fromCssColorString(drone.color).withAlpha(0.6),
            dashLength: 8,
          }),
        },
        // Visible point marker
        point: {
          pixelSize: 16,
          color: Cesium.Color.fromCssColorString(drone.color),
          outlineColor: Cesium.Color.WHITE,
          outlineWidth: 2,
          disableDepthTestDistance: Number.POSITIVE_INFINITY,
        },
        // Clean, readable text label with background pill
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

    // 5. Explicit Click Event Handler for picking drone markers directly on the globe
    const clickHandler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)
    clickHandler.setInputAction((movement) => {
      const pickedObject = viewer.scene.pick(movement.position)
      if (Cesium.defined(pickedObject) && pickedObject.id) {
        const drone = STATIC_DRONES.find((d) => d.droneId === pickedObject.id.id)
        if (drone) {
          setSelectedDrone(drone)
          return
        }
      }
      // Clicked on empty space
      setSelectedDrone(null)
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)

    // 6. Initial Camera focus encompassing all 5 drone waypoints in Hyderabad
    viewer.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(78.4350, 17.4150, 26000),
      orientation: {
        heading: Cesium.Math.toRadians(0),
        pitch: Cesium.Math.toRadians(-45),
        roll: 0.0,
      },
      duration: 1.8,
    })

    // 7. Cleanup on unmount (essential for React 19 StrictMode)
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

  // Basic navigation controls
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
    setSelectedDrone(drone)
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
          <span className="mission-hud-badge">Day 3</span>
        </div>
        <div className="mission-hud-sub">
          Static Drone Markers • Hyderabad Hub
        </div>

        <div className="mission-drone-list">
          {STATIC_DRONES.map((drone) => (
            <div
              key={drone.droneId}
              className={`drone-card ${selectedDrone?.droneId === drone.droneId ? 'active' : ''}`}
              style={{ borderLeftColor: drone.color }}
              onClick={() => handleFlyToDrone(drone)}
              title="Click to focus camera and view info"
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

      {/* Dedicated Drone Information Popup */}
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
              onClick={() => setSelectedDrone(null)}
              title="Close Popup"
              aria-label="Close"
            >
              ✕
            </button>
          </div>

          <div className="drone-popup-subtitle">{selectedDrone.name}</div>

          <div className="drone-popup-grid">
            <div className="drone-popup-row">
              <span className="field-label">Mission Status:</span>
              <span className="field-value status-badge">{selectedDrone.status}</span>
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
              <span className="field-label">Coordinates:</span>
              <code className="coords-code">
                {selectedDrone.lat.toFixed(4)}° N, {selectedDrone.lon.toFixed(4)}° E
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
