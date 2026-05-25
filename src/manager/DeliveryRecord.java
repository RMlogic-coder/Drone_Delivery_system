package manager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DeliveryRecord {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime timestamp;
    private final int packageId;
    private final int droneId;
    private final String droneType;
    private final String destination;
    private final double distance;
    private final double estimatedMinutes;
    private final double batteryUsed;
    private final String outcome;

    public DeliveryRecord(int packageId, int droneId, String droneType, String destination,
                          double distance, double estimatedMinutes, double batteryUsed, String outcome) {
        this.timestamp = LocalDateTime.now();
        this.packageId = packageId;
        this.droneId = droneId;
        this.droneType = droneType;
        this.destination = destination;
        this.distance = distance;
        this.estimatedMinutes = estimatedMinutes;
        this.batteryUsed = batteryUsed;
        this.outcome = outcome;
    }

    public String toCsvLine() {
        return timestamp.format(FORMATTER) + "," + packageId + "," + droneId + "," + droneType + ","
                + destination + "," + String.format("%.2f", distance) + ","
                + String.format("%.2f", estimatedMinutes) + "," + String.format("%.2f", batteryUsed) + "," + outcome;
    }

    @Override
    public String toString() {
        return "[" + timestamp.format(FORMATTER) + "] Package " + packageId
                + " handled by Drone " + droneId + " (" + droneType + ")"
                + " | destination=" + destination
                + " | distance=" + String.format("%.2f", distance)
                + " | eta=" + String.format("%.2f", estimatedMinutes) + " min"
                + " | batteryUsed=" + String.format("%.2f", batteryUsed) + "%"
                + " | outcome=" + outcome;
    }
}