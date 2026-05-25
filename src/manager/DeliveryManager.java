
// ==========================================
// File: manager/DeliveryManager.java
// ==========================================
package manager;

import components.Location;
import drones.Drone;
import drones.ExpressDrone;
import drones.HeavyLiftDrone;
import drones.StandardDrone;
import exceptions.BatteryInsufficientException;
import exceptions.DuplicateIdException;
import exceptions.DroneUnavailableException;
import exceptions.PackageOverweightException;
import exceptions.ValidationException;
import util.BackendLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DeliveryManager {

    private final List<Drone> drones;
    private final List<DeliveryRecord> deliveryHistory;
    private final Set<Integer> droneIds;
    private final Set<Integer> packageIds;
    private final Set<String> reservedDestinations;
    private final Object fleetLock;

    // Warehouse — all drones start here, all packages picked up from here
    private static final Location WAREHOUSE = new Location(0, 0);
    private static final Path HISTORY_FILE = Paths.get(System.getProperty("user.dir"), "delivery-history.csv");

    public DeliveryManager() {
        drones = new ArrayList<>();
        deliveryHistory = new ArrayList<>();
        droneIds = new HashSet<>();
        packageIds = new HashSet<>();
        reservedDestinations = new HashSet<>();
        fleetLock = new Object();
    }

    // Add drone to fleet
    public void addDrone(Drone drone) {
        if (drone == null) {
            throw new ValidationException("Drone cannot be null.");
        }

        synchronized (fleetLock) {
            if (droneIds.contains(drone.getDroneId())) {
                throw new DuplicateIdException("Duplicate drone ID detected: " + drone.getDroneId());
            }
            drones.add(drone);
            droneIds.add(drone.getDroneId());
        }

        BackendLogger.info("Registered drone " + drone.getDroneId() + " (" + drone.getClass().getSimpleName() + ").");
    }

    // Show all drones
    public void showAllDrones() {
        System.out.println("\n========== DRONE FLEET ==========");
        synchronized (fleetLock) {
            for (Drone d : drones) {
                d.showStatus();
            }
        }
    }

    // =============================================
    // DISTANCE CALCULATION — Member 2 owns this
    // Always from WAREHOUSE (0,0) to destination
    // =============================================
    public double calculateDistance(Location destination) {
        if (destination == null) {
            throw new ValidationException("Destination cannot be null.");
        }
        return WAREHOUSE.distanceTo(destination);
    }

    private String normalizePreferredType(String preferredType) {
        if (preferredType == null || preferredType.trim().isEmpty()) {
            return "ANY";
        }
        return preferredType.trim().toUpperCase(Locale.ROOT);
    }

    private String destinationKey(Location destination) {
        return destination.toString();
    }

    private void reserveDestination(Location destination) {
        if (destination == null) {
            throw new ValidationException("Destination cannot be null.");
        }
        synchronized (fleetLock) {
            String key = destinationKey(destination);
            if (reservedDestinations.contains(key)) {
                throw new DroneUnavailableException("Another drone is already scheduled for destination " + destination + ".");
            }
            reservedDestinations.add(key);
        }
    }

    private void releaseDestination(Location destination) {
        synchronized (fleetLock) {
            reservedDestinations.remove(destinationKey(destination));
        }
    }

    private void validatePackageRegistry(DeliveryPackage pkg) {
        if (pkg == null) {
            throw new ValidationException("Delivery package cannot be null.");
        }
        if (pkg.getWeight() <= 0) {
            throw new ValidationException("Invalid package weight.");
        }
        synchronized (fleetLock) {
            if (packageIds.contains(pkg.getPackageId())) {
                throw new DuplicateIdException("Duplicate package ID detected: " + pkg.getPackageId());
            }
            packageIds.add(pkg.getPackageId());
        }
    }

    private void validateWeightAgainstFleet(DeliveryPackage pkg) {
        double maxCapacity = 0.0;
        synchronized (fleetLock) {
            for (Drone drone : drones) {
                if (drone.getCapacity() > maxCapacity) {
                    maxCapacity = drone.getCapacity();
                }
            }
        }
        if (pkg.getWeight() > maxCapacity) {
            throw new PackageOverweightException("Package weight exceeds drone capacity.");
        }
    }

    private void releasePackageId(DeliveryPackage pkg) {
        synchronized (fleetLock) {
            packageIds.remove(pkg.getPackageId());
        }
    }

    private Drone selectBestDrone(DeliveryPackage pkg, String preferredType) {
        Drone bestDrone = null;
        double bestBattery = -1;

        synchronized (fleetLock) {
            for (Drone drone : drones) {
                if (!drone.isAvailable()) {
                    continue;
                }
                if (drone.getCapacity() < pkg.getWeight()) {
                    continue;
                }
                if (!drone.canCompleteRoundTrip(pkg.getDestination())) {
                    BackendLogger.warn("Drone " + drone.getDroneId() + " skipped because battery is insufficient for the round trip.");
                    continue;
                }

                boolean preferredMatch = "ANY".equals(preferredType)
                        || ("STANDARD".equals(preferredType) && drone instanceof StandardDrone)
                        || ("EXPRESS".equals(preferredType) && drone instanceof ExpressDrone)
                        || ("HEAVY".equals(preferredType) && drone instanceof HeavyLiftDrone);

                if (!preferredMatch) {
                    continue;
                }

                if (pkg.getPriority().equals("URGENT") && "ANY".equals(preferredType) && !(drone instanceof ExpressDrone)) {
                    continue;
                }

                if (drone.getBattery().getLevel() > bestBattery) {
                    bestBattery = drone.getBattery().getLevel();
                    bestDrone = drone;
                }
            }
        }

        if (bestDrone == null && pkg.getPriority().equals("URGENT")) {
            synchronized (fleetLock) {
                for (Drone drone : drones) {
                    if (!drone.isAvailable()) {
                        continue;
                    }
                    if (drone.getCapacity() < pkg.getWeight()) {
                        continue;
                    }
                    if (!drone.canCompleteRoundTrip(pkg.getDestination())) {
                        continue;
                    }
                    if (drone.getBattery().getLevel() > bestBattery) {
                        bestBattery = drone.getBattery().getLevel();
                        bestDrone = drone;
                    }
                }
            }
        }

        return bestDrone;
    }

    // =============================================
    // MAIN ASSIGNMENT LOGIC
    // Selects best drone based on:
    // 1. Availability
    // 2. Weight capacity
    // 3. Battery level
    // 4. Drone type (URGENT = ExpressDrone)
    // =============================================
    public Drone assignDrone(DeliveryPackage pkg, String preferredType) {
        validatePackageRegistry(pkg);
        validateWeightAgainstFleet(pkg);

        String normalizedType = normalizePreferredType(preferredType);
        double distance = calculateDistance(pkg.getDestination());

        BackendLogger.info("Searching drone for package " + pkg.getPackageId()
                + " | priority=" + pkg.getPriority()
                + " | weight=" + pkg.getWeight() + " kg"
                + " | distance=" + String.format("%.2f", distance));

        Drone bestDrone = selectBestDrone(pkg, normalizedType);
        if (bestDrone == null) {
            BackendLogger.warn("No suitable drone found for package " + pkg.getPackageId() + ".");
            return null;
        }

        if (!bestDrone.canCompleteRoundTrip(pkg.getDestination())) {
            throw new BatteryInsufficientException("Drone " + bestDrone.getDroneId()
                    + " cannot complete the round trip with the safety buffer.");
        }

        bestDrone.assignPackage(pkg);
        BackendLogger.info("Assigned package " + pkg.getPackageId() + " to drone " + bestDrone.getDroneId()
                + " (" + bestDrone.getClass().getSimpleName() + ")");
        return bestDrone;
    }

    public boolean processDelivery(DeliveryPackage pkg, String preferredType) {
        if (pkg == null) {
            throw new ValidationException("Delivery package cannot be null.");
        }
        Drone assignedDrone = null;
        reserveDestination(pkg.getDestination());
        try {
            assignedDrone = assignDrone(pkg, preferredType);
            if (assignedDrone == null) {
                releasePackageId(pkg);
                return false;
            }

            double distance = calculateDistance(pkg.getDestination());
            double estimatedMinutes = assignedDrone.estimateDeliveryMinutes(pkg.getDestination());
            double totalConsumption = assignedDrone.estimateConsumption(pkg.getDestination());

            assignedDrone.flyTo(pkg.getDestination());
            assignedDrone.deliverPackage();
            assignedDrone.returnToBase();

            DeliveryRecord record = new DeliveryRecord(
                    pkg.getPackageId(),
                    assignedDrone.getDroneId(),
                    assignedDrone.getClass().getSimpleName(),
                    pkg.getDestination().toString(),
                    distance,
                    estimatedMinutes,
                    totalConsumption,
                    "COMPLETED"
            );

            synchronized (fleetLock) {
                deliveryHistory.add(record);
            }
            appendRecord(record);
            System.out.println(record);
            return true;
        } catch (RuntimeException ex) {
            BackendLogger.error(ex.getMessage());
            if (assignedDrone != null) {
                assignedDrone.cancelAssignment();
            }
            releasePackageId(pkg);
            return false;
        } finally {
            releaseDestination(pkg.getDestination());
        }
    }

    public List<DeliveryRecord> getDeliveryHistory() {
        synchronized (fleetLock) {
            return new ArrayList<>(deliveryHistory);
        }
    }

    private void appendRecord(DeliveryRecord record) {
        try {
            Files.write(
                    HISTORY_FILE,
                    (record.toCsvLine() + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ex) {
            BackendLogger.warn("Unable to persist delivery history: " + ex.getMessage());
        }
    }
}