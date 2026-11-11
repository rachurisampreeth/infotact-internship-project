
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SimulatorChecks {
    public static void main(String[] args) throws Exception {
        Process process = new ProcessBuilder(
            "java",
            "simulator/MockDroneSimulator.java",
            "--drones", "2",
            "--duration", "1",
            "--interval", "0.1"
        ).redirectErrorStream(true).start();

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new AssertionError("Simulator exited with " + exitCode);
        }

        long droneOneCount = lines.stream()
            .filter(line -> line.contains("\"droneId\":\"drone-001\""))
            .count();
        long droneTwoCount = lines.stream()
            .filter(line -> line.contains("\"droneId\":\"drone-002\""))
            .count();

        if (droneOneCount == 0 || droneTwoCount == 0) {
            throw new AssertionError("Expected telemetry for both drones");
        }

        for (String line : lines) {
            if (line.startsWith("{")
                    && (!line.contains("\"lat\":")
                    || !line.contains("\"lon\":")
                    || !line.contains("\"alt\":")
                    || !line.contains("\"battery\":")
                    || !line.contains("\"step\":")
                    || !line.contains("\"status\":")
                    || !line.contains("\"timestamp\":"))) {
                throw new AssertionError("Missing telemetry field: " + line);
            }
        }

        System.out.println("PASS: simulator produced telemetry for both drones.");
        System.out.println("PASS: telemetry includes required field names.");
        Process invalidProcess = new ProcessBuilder(
        "java",
        "simulator/MockDroneSimulator.java",
        "--drones", "51",
        "--duration", "1"
).redirectErrorStream(true).start();

String invalidOutput;
try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(invalidProcess.getInputStream()))) {
    invalidOutput = reader.lines()
            .reduce("", (a, b) -> a + b + "\n");
}

int invalidExitCode = invalidProcess.waitFor();

if (invalidExitCode != 0 ||
        !invalidOutput.contains("Use 1-50 drones")) {
    throw new AssertionError(
            "Simulator did not handle invalid drone count correctly: "
                    + invalidOutput);
}

System.out.println("PASS: simulator rejects more than 50 drones.");
    }
}
