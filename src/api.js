// src/api.js – lightweight API helper for AeroSageX backend
// Uses Vite env variable VITE_API_BASE if provided, otherwise falls back to localhost:8081

const DEFAULT_API_BASE = 'http://localhost:8081/api';

export const getApiBase = () => {
  const envBase = import.meta?.env?.VITE_API_BASE;
  if (envBase && typeof envBase === 'string' && envBase.trim() !== '') {
    // Trim any trailing slash
    return envBase.replace(/\/+$/, '');
  }
  return DEFAULT_API_BASE;
};

/**
 * Fetch mission state for a given droneId.
 * @param {string} droneId - e.g. "DR-01" or "drone-001" (backend expects the exact ID)
 * @param {string|null} fleetId - optional query param
 * @param {AbortSignal} signal - optional abort signal for fetch cancellation
 * @returns {Promise<{workflowId:string, droneId:string, state:string}>}
 */
export async function fetchMissionState(droneId, fleetId = null, signal) {
  if (!droneId) {
    throw new Error('Missing droneId');
  }
  const base = getApiBase();
  const url = new URL(`${base}/missions/${encodeURIComponent(droneId)}/state`);
  if (fleetId) {
    url.searchParams.set('fleetId', fleetId);
  }
  const response = await fetch(url.toString(), {
    method: 'GET',
    headers: {
      'Accept': 'application/json',
    },
    signal,
    // The backend has CORS configured for http://localhost:5173, which matches Vite dev server.
    credentials: 'omit',
  });

  if (!response.ok) {
    if (response.status === 404) {
      // Return null to indicate mission not found – caller can show a friendly message.
      return null;
    }
    const text = await response.text();
    throw new Error(`Mission fetch failed (${response.status}): ${text}`);
  }
  const data = await response.json();
  // Basic validation – ensure required fields exist
  if (!data || typeof data !== 'object' || !data.droneId) {
    throw new Error('Invalid mission response format');
  }
  return data;
}
