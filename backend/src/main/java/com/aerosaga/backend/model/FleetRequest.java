package com.aerosaga.backend.model;

import java.util.List;

public class FleetRequest {

    private String fleetId;
    private List<MissionRequest> drones;

    public FleetRequest() {
    }

    public String getFleetId() {
        return fleetId;
    }

    public void setFleetId(String fleetId) {
        this.fleetId = fleetId;
    }

    public List<MissionRequest> getDrones() {
        return drones;
    }

    public void setDrones(List<MissionRequest> drones) {
        this.drones = drones;
    }
}
