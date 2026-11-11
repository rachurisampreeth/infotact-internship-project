
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Locale;

public class MockDroneSimulator {
    private static final String[] STEPS = {
        "TAKEOFF", "NAVIGATE", "DROP", "RETURN"
    };

    private static final String TELEMETRY_URL =
        "http://localhost:8081/api/telemetry";

    public static void main(String[] args) throws Exception {
        int drones = 10;
        int duration = 60;
        double interval = 1.0;
        boolean send = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--drones") || args[i].equals("--duration")
            || args[i].equals("--interval")) {
        if (i + 1 >= args.length) {
            System.err.println("Missing value after " + args[i]);
            return;
        }
    }
            switch (args[i]) {
                case "--drones" -> {
    try {
        drones = Integer.parseInt(args[++i]);
    } catch (NumberFormatException e) {
        System.err.println("Invalid number for --drones. Use an integer from 1 to 50.");
        return;
    }
}
                case "--duration" -> {
    try {
        duration = Integer.parseInt(args[++i]);
    } catch (NumberFormatException e) {
        System.err.println("Invalid number for --duration. Use an integer of at least 1.");
        return;
    }
}
case "--interval" -> {
    try {
        interval = Double.parseDouble(args[++i]);
    } catch (NumberFormatException e) {
        System.err.println("Invalid number for --interval. Use a positive number.");
        return;
    }
}
case "--send" -> send = true;
                case "--help" -> {
                    System.out.println(
                        "Usage: java simulator/MockDroneSimulator.java "
                        + "[--drones 1-50] [--duration seconds] "
                        + "[--interval seconds] [--send]"
                    );
                    return;
                }
                default -> {
                    System.err.println("Unknown argument: " + args[i]);
                    return;
                }
            }
        }

        if (drones < 1 || drones > 50 || duration < 1
                || !Double.isFinite(interval) || interval <= 0) {
            System.err.println(
                "Use 1-50 drones, duration >= 1 second, "
                + "and a positive interval."
            );
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        long start = System.nanoTime();
        long durationNanos = duration * 1_000_000_000L;
        int tick = 0;

        System.out.printf(
            "Starting %d mock drone(s) for %d seconds.%n",
            drones, duration
        );
        if (send) {
            System.out.println("Backend sending enabled: " + TELEMETRY_URL);
        } else {
            System.out.println("Print-only mode; no backend requests will be sent.");
        }

        try {
            while (System.nanoTime() - start < durationNanos) {
                for (int d = 1; d <= drones; d++) {
                    String id = String.format(Locale.ROOT, "drone-%03d", d);
                    int phase = Math.min(tick, STEPS.length - 1);
                    String step = STEPS[phase];
                    boolean complete = tick >= STEPS.length;
                    String status = complete ? "COMPLETED" : "ACTIVE";

                    double progress = Math.min(tick / 3.0, 1.0);
                    double lat = 17.4000 + d * 0.0001 + progress * 0.005;
                    double lon = 78.4000 + d * 0.0001 + progress * 0.005;
                    double altitude = switch (step) {
                        case "TAKEOFF" -> 10.0;
                        case "NAVIGATE", "DROP" -> 120.0;
                        default -> 0.0;
                    };
                    int battery = Math.max(0, 100 - tick);

                    String json = String.format(
                        Locale.ROOT,
                        "{\"droneId\":\"%s\",\"lat\":%.6f,"
                        + "\"lon\":%.6f,\"alt\":%.1f,\"battery\":%d,"
                        + "\"step\":\"%s\",\"status\":\"%s\","
                        + "\"timestamp\":%d}",
                        id, lat, lon, altitude, battery, step, status,
                        Instant.now().toEpochMilli()
                    );

                    System.out.println(json);

                    if (send) {
                        HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(TELEMETRY_URL))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

                        try {
                            HttpResponse<String> response = client.send(
                                request, HttpResponse.BodyHandlers.ofString()
                            );
                            if (response.statusCode() < 200
                                    || response.statusCode() >= 300) {
                                System.err.printf(
                                    "Backend rejected %s: HTTP %d%n",
                                    id, response.statusCode()
                                );
                            }
                        } catch (Exception e) {
                            System.err.printf(
                                "Could not send %s to backend: %s%n",
                                id, e.getMessage()
                            );
                        }
                    }
                }

                tick++;
                long elapsed = System.nanoTime() - start;
                if (elapsed < durationNanos) {
                    long sleepMillis = (long) (interval * 1000);
                    Thread.sleep(Math.max(1, sleepMillis));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Simulator stopped.");
        }

        System.out.println("Simulation finished.");
    }
}
