
package drones;

import components.Battery;
import components.Location;
import exceptions.BatteryInsufficientException;
import exceptions.InvalidDroneStateException;
import exceptions.ValidationException;
import manager.DeliveryPackage;
import util.BackendLogger;

public abstract class Drone {
    private static final Location WAREHOUSE = new Location(0, 0);
    protected static final double SAFETY_BUFFER = 5.0;
    protected static final double CRITICAL_BATTERY_LEVEL = 15.0;

    protected final int droneId;
    protected final double capacity;
    protected final Battery battery;
    protected Location location;
    protected String status;
    protected DeliveryPackage activePackage;
    private double lastFlightConsumption;

    public Drone(int droneId, double capacity) {
        if (droneId <= 0) {
            throw new IllegalArgumentException("Drone ID must be positive.");
        }
        if (!Double.isFinite(capacity) || capacity <= 0) {
            throw new IllegalArgumentException("Drone capacity must be a positive number.");
        }
        this.droneId = droneId;
        this.capacity = capacity;
        this.battery = new Battery(100);
        this.location = new Location(0, 0);
        this.status = "AVAILABLE";
    }

    protected abstract double getConsumptionRatePerUnit();

    protected abstract double getSpeedUnitsPerMinute();

    /**
     * Estimates the full round-trip battery consumption from the drone's current location to the
     * destination and back to the warehouse.
     */
    public synchronized double estimateConsumption(Location destination) {
        validateDestination(destination);
        double outboundDistance = location.distanceTo(destination);
        double returnDistance = destination.distanceTo(WAREHOUSE);
        return (outboundDistance + returnDistance) * getConsumptionRatePerUnit();
    }

    /**
     * Estimates the one-way delivery time in minutes.
     */
    public synchronized double estimateDeliveryMinutes(Location destination) {
        validateDestination(destination);
        return location.distanceTo(destination) / getSpeedUnitsPerMinute();
    }

    public synchronized double estimateOneWayConsumption(Location destination) {
        validateDestination(destination);
        return location.distanceTo(destination) * getConsumptionRatePerUnit();
    }

    public synchronized void assignPackage(DeliveryPackage deliveryPackage) {
        if (deliveryPackage == null) {
            throw new ValidationException("Delivery package cannot be null.");
        }
        if (!"AVAILABLE".equals(status)) {
            throw new InvalidDroneStateException("Drone " + droneId + " is not available for assignment.");
        }
        activePackage = deliveryPackage;
        status = "ASSIGNED";
    }

    /**
     * Moves the drone to the destination after validating state and battery budget.
     */
    public synchronized void flyTo(Location destination) {
        validateDestination(destination);
        ensureFlightAllowed();

        double consumption = estimateOneWayConsumption(destination);
        BackendLogger.info("Drone " + droneId + " flying from " + location + " to " + destination
                + " | estimated battery use=" + String.format("%.2f", consumption) + "%");

        if (!battery.hasSufficientCharge(consumption + SAFETY_BUFFER)) {
            throw new BatteryInsufficientException("Drone " + droneId
                    + " has insufficient battery for the flight and safety buffer.");
        }

        status = destination.equals(WAREHOUSE) ? "RETURNING" : "DELIVERING";
        battery.drainBattery(consumption);
        lastFlightConsumption = consumption;
        location = destination;

        BackendLogger.info("Drone " + droneId + " arrived at " + destination
                + " | battery remaining=" + String.format("%.2f", battery.getLevel()) + "%");

        if (battery.getLevel() <= CRITICAL_BATTERY_LEVEL) {
            BackendLogger.warn("Drone " + droneId + " battery is critically low at "
                    + String.format("%.2f", battery.getLevel()) + "%.");
        }
    }

    // Deliver package at current location
    public synchronized void deliverPackage() {
        if (activePackage == null) {
            throw new InvalidDroneStateException("Drone " + droneId + " has no assigned package to deliver.");
        }
        if (!location.equals(activePackage.getDestination())) {
            throw new InvalidDroneStateException("Drone " + droneId + " is not at the delivery destination.");
        }

        status = "DELIVERING";
        activePackage.setDelivered(true);
        BackendLogger.info("Drone " + droneId + " delivered package " + activePackage.getPackageId()
                + " at " + location + " | battery remaining=" + String.format("%.2f", battery.getLevel()) + "%");
        activePackage = null;
        status = "DELIVERED";
    }

    // Return drone back to base (0,0)
    public synchronized void returnToBase() {
        if (activePackage != null) {
            throw new InvalidDroneStateException("Drone " + droneId + " cannot return before delivery is completed.");
        }
        if (location.equals(WAREHOUSE) && "AVAILABLE".equals(status)) {
            BackendLogger.info("Drone " + droneId + " is already at the warehouse.");
            return;
        }

        BackendLogger.info("Drone " + droneId + " returning to base.");
        status = "RETURNING";
        flyTo(new Location(0, 0));
        status = "AVAILABLE";

        BackendLogger.info("Drone " + droneId + " is back at base and available.");
        autoRechargeIfCritical();
    }

    // Charge drone battery
    public synchronized void charge() {
        status = "CHARGING";
        battery.chargeBattery();
        BackendLogger.info("Drone " + droneId + " recharge completed.");
        status = "AVAILABLE";
    }

    public synchronized void autoRechargeIfCritical() {
        if (battery.getLevel() <= CRITICAL_BATTERY_LEVEL) {
            BackendLogger.warn("Drone " + droneId + " auto-charging due to low battery.");
            charge();
        }
    }

    public synchronized boolean canCompleteRoundTrip(Location destination) {
        return battery.hasSufficientCharge(estimateConsumption(destination) + SAFETY_BUFFER);
    }

    public synchronized void cancelAssignment() {
        activePackage = null;
        status = "AVAILABLE";
    }

    // Print current drone status
    public synchronized void showStatus() {
        System.out.println("\n----- Drone Status -----");
        System.out.println("Drone ID  : " + droneId);
        System.out.println("Type      : " + this.getClass().getSimpleName());
        System.out.println("Capacity  : " + capacity + " kg");
        System.out.println("Battery   : " + battery.getLevel() + "%");
        System.out.println("Location  : " + location);
        System.out.println("Status    : " + status);
        if (activePackage != null) {
            System.out.println("Package   : " + activePackage.getPackageId());
        }
    }

    public synchronized double getLastFlightConsumption() {
        return lastFlightConsumption;
    }

    public synchronized boolean isAvailable() {
        return "AVAILABLE".equals(status);
    }

    protected void validateDestination(Location destination) {
        if (destination == null) {
            throw new ValidationException("Destination cannot be null.");
        }
    }

    protected void ensureFlightAllowed() {
        if ("CHARGING".equals(status)) {
            throw new InvalidDroneStateException("Drone " + droneId + " is currently charging.");
        }
        if ("AVAILABLE".equals(status)) {
            throw new InvalidDroneStateException("Drone " + droneId + " must be assigned before flying.");
        }
    }

    // Getters
    public int getDroneId() { return droneId; }
    public double getCapacity() { return capacity; }
    public Battery getBattery() { return battery; }
    public Location getLocation() { return location; }
    public String getStatus() { return status; }

    // Setters
    public synchronized void setStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new ValidationException("Drone status cannot be null or empty.");
        }
        this.status = status.trim().toUpperCase();
    }

    public synchronized void setLocation(Location location) {
        validateDestination(location);
        this.location = location;
    }
}