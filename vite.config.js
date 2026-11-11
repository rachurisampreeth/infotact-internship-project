import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import path from 'path'
import fs from 'fs'

function cesiumAssetsPlugin() {
  return {
    name: 'cesium-assets-plugin',
    configResolved() {
      const cesiumSource = path.resolve('node_modules/cesium/Build/Cesium')
      const targetDir = path.resolve('public/cesium')
      const folders = ['Assets', 'ThirdParty', 'Widgets', 'Workers']
      for (const folder of folders) {
        const dest = path.join(targetDir, folder)
        if (!fs.existsSync(dest)) {
          fs.cpSync(path.join(cesiumSource, folder), dest, { recursive: true })
        }
      }
    },
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), cesiumAssetsPlugin()],
  define: {
    CESIUM_BASE_URL: JSON.stringify('/cesium'),
  },
})
