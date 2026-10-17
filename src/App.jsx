import { useEffect, useRef } from 'react'
import * as Cesium from 'cesium'
import 'cesium/Build/Cesium/Widgets/widgets.css'
import './App.css'

// Ensure Cesium static assets are resolved from /cesium
if (typeof window !== 'undefined') {
  window.CESIUM_BASE_URL = '/cesium'
}

// Static mock drones conforming to CONTRACTS.md
// Coordinates order in contracts is lat, lon. Cesium requires lon, lat!
const STATIC_DRONES = [
  {
    droneId: 'drone-001',
    lat: 17.4000,
    lon: 78.4000,
    alt: 120.0,
    battery: 92,
    step: 'NAVIGATE',
    status: 'ACTIVE',
  },
  {
    droneId: 'drone-002',
    lat: 17.4250,
    lon: 78.4350,
    alt: 150.0,
    battery: 78,
    step: 'TAKEOFF',
    status: 'ACTIVE',
  },
  {
    droneId: 'drone-003',
    lat: 17.3850,
    lon: 78.3700,
    alt: 95.0,
    battery: 64,
    step: 'DROP',
    status: 'ACTIVE',
  },
]

function App() {
  const containerRef = useRef(null)
  const viewerRef = useRef(null)

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
      infoBox: true,
      selectionIndicator: true,
    })

    viewerRef.current = viewer

    // 4. Add static drone markers with labels
    STATIC_DRONES.forEach((drone) => {
      // NOTE: Cesium expects lon first: fromDegrees(lon, lat, alt)
      const position = Cesium.Cartesian3.fromDegrees(
        drone.lon,
        drone.lat,
        drone.alt
      )

      viewer.entities.add({
        id: drone.droneId,
        name: `${drone.droneId} [${drone.step}]`,
        position: position,
        point: {
          pixelSize: 14,
          color: Cesium.Color.fromCssColorString('#f0883e'),
          outlineColor: Cesium.Color.WHITE,
          outlineWidth: 2,
          disableDepthTestDistance: Number.POSITIVE_INFINITY,
        },
        label: {
          text: `${drone.droneId}\n[${drone.step}] • ${drone.alt}m`,
          font: 'bold 12px sans-serif',
          fillColor: Cesium.Color.WHITE,
          outlineColor: Cesium.Color.BLACK,
          outlineWidth: 3,
          style: Cesium.LabelStyle.FILL_AND_OUTLINE,
          verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
          pixelOffset: new Cesium.Cartesian2(0, -18),
          disableDepthTestDistance: Number.POSITIVE_INFINITY,
        },
        description: `
          <div style="font-family: -apple-system, BlinkMacSystemFont, sans-serif; padding: 10px; color: #1f2328;">
            <h3 style="margin: 0 0 8px 0; color: #0969da;">${drone.droneId}</h3>
            <table style="width: 100%; font-size: 13px; border-collapse: collapse;">
              <tr><td style="padding: 3px 0; color: #656d76;">Status:</td><td><strong>${drone.status}</strong></td></tr>
              <tr><td style="padding: 3px 0; color: #656d76;">Step:</td><td><strong>${drone.step}</strong></td></tr>
              <tr><td style="padding: 3px 0; color: #656d76;">Altitude:</td><td><strong>${drone.alt} m</strong></td></tr>
              <tr><td style="padding: 3px 0; color: #656d76;">Battery:</td><td><strong>${drone.battery}%</strong></td></tr>
              <tr><td style="padding: 3px 0; color: #656d76;">Lat / Lon:</td><td><code>${drone.lat.toFixed(4)}, ${drone.lon.toFixed(4)}</code></td></tr>
            </table>
          </div>
        `,
      })
    })

    // 5. Initial Camera focus on Hyderabad depot (17.4° N, 78.4° E) with 3D tilt
    viewer.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(78.4000, 17.4000, 14000),
      orientation: {
        heading: Cesium.Math.toRadians(0),
        pitch: Cesium.Math.toRadians(-45),
        roll: 0.0,
      },
      duration: 1.8,
    })

    // 6. Cleanup on unmount (essential for React 19 StrictMode)
    return () => {
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
    const height = camera.positionCartographic.height
    camera.dollyIn(height * 0.35)
  }

  const handleZoomOut = () => {
    if (!viewerRef.current) return
    const camera = viewerRef.current.camera
    const height = camera.positionCartographic.height
    camera.dollyOut(height * 0.35)
  }

  const handleResetCamera = () => {
    if (!viewerRef.current) return
    viewerRef.current.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(78.4000, 17.4000, 14000),
      orientation: {
        heading: Cesium.Math.toRadians(0),
        pitch: Cesium.Math.toRadians(-45),
        roll: 0.0,
      },
      duration: 1.2,
    })
  }

  const handleFlyToDrone = (drone) => {
    if (!viewerRef.current) return
    viewerRef.current.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(drone.lon, drone.lat, 2200),
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
          <span className="mission-hud-badge">Day 2</span>
        </div>
        <div className="mission-hud-sub">
          Interactive 3D CesiumJS Earth Globe
        </div>

        <div className="mission-drone-list">
          {STATIC_DRONES.map((drone) => (
            <div
              key={drone.droneId}
              className="drone-card"
              onClick={() => handleFlyToDrone(drone)}
              title="Click to focus camera on drone"
            >
              <span className="drone-id">{drone.droneId}</span>
              <span className="drone-step">{drone.step}</span>
            </div>
          ))}
        </div>
      </div>

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
          title="Reset Camera to Mission Depot"
          aria-label="Reset Camera"
        >
          ⌂
        </button>
      </div>

      {/* Camera Gesture Guide */}
      <div className="help-bar">
        <div className="help-item"><span>Left Drag:</span> Rotate/Pan</div>
        <div className="help-item"><span>Right Drag / Scroll:</span> Zoom</div>
        <div className="help-item"><span>Middle Drag:</span> Tilt Camera</div>
      </div>
    </div>
  )
}

export default App
