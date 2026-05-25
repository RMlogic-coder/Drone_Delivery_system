
// File: drones/HeavyLiftDrone.java

package drones;

public class HeavyLiftDrone extends Drone {
    private static final double CONSUMPTION_PER_UNIT = 2.0; // percent per unit distance
    private static final double SPEED_UNITS_PER_MINUTE = 8.0;

    public HeavyLiftDrone(int droneId) {
        super(droneId, 20.0); // Max capacity: 20 kg
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
