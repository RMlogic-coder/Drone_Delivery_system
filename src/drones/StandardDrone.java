// ======================================
// File: drones/StandardDrone.java
// ======================================
package drones;

public class StandardDrone extends Drone {
    private static final double CONSUMPTION_PER_UNIT = 1.0; // percent per unit distance
    private static final double SPEED_UNITS_PER_MINUTE = 12.0;

    public StandardDrone(int droneId) {
        super(droneId, 5.0); // Max capacity: 5 kg
    }

    @Override
    protected double getConsumptionRatePerUnit() {
        return CONSUMPTION_PER_UNIT;
    }

    @Override
    protected double getSpeedUnitsPerMinute() {
        return SPEED_UNITS_PER_MINUTE;
    }
}