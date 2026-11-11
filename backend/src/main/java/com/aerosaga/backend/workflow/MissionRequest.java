
package com.aerosaga.backend.workflow;

public class MissionRequest {

    public String droneId;
    public double destLat;
    public double destLon;
    public String parentWorkflowId;

    public MissionRequest() {
    }

    public MissionRequest(String droneId, double destLat, double destLon) {
        this.droneId = droneId;
        this.destLat = destLat;
        this.destLon = destLon;
    }

    public MissionRequest(
            String droneId,
            double destLat,
            double destLon,
            String parentWorkflowId) {

        this.droneId = droneId;
        this.destLat = destLat;
        this.destLon = destLon;
        this.parentWorkflowId = parentWorkflowId;
    }
}
