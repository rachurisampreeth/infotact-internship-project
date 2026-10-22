import { useEffect, useState, useRef } from 'react';
import { fetchMissionState } from '../api';

/**
 * Hook to fetch a mission state for a given droneId.
 * Returns { mission, loading, error }.
 * Caches results per droneId to avoid duplicate network calls.
 */
export function useMissionState(droneId, fleetId = null) {
  const [mission, setMission] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const cacheRef = useRef(new Map());

  useEffect(() => {
    if (!droneId) {
      setMission(null);
      setError(null);
      setLoading(false);
      return;
    }

    // Return cached mission if present
    if (cacheRef.current.has(droneId)) {
      setMission(cacheRef.current.get(droneId));
      return;
    }

    const controller = new AbortController();
    setLoading(true);
    setError(null);

    fetchMissionState(droneId, fleetId, controller.signal)
      .then((data) => {
        cacheRef.current.set(droneId, data);
        setMission(data);
      })
      .catch((err) => {
        if (err.name !== 'AbortError') {
          setError(err.message);
        }
      })
      .finally(() => setLoading(false));

    return () => {
      controller.abort();
    };
  }, [droneId, fleetId]);

  return { mission, loading, error };
}
