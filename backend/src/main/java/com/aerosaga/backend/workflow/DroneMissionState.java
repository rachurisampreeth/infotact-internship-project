
package com.aerosaga.backend.workflow;

public class DroneMissionState {

    public String droneId;
    public String currentStep;
    public String status;

    public DroneMissionState() {
    }

    public DroneMissionState(
            String droneId,
            String currentStep,
            String status
    ) {
        this.droneId = droneId;
        this.currentStep = currentStep;
        this.status = status;
    }
}
