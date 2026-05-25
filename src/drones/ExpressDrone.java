
// File: drones/ExpressDrone.java

package drones;

public class ExpressDrone extends Drone {
    private static final double CONSUMPTION_PER_UNIT = 1.5; // percent per unit distance
    private static final double SPEED_UNITS_PER_MINUTE = 18.0;

    public ExpressDrone(int droneId) {
        super(droneId, 3.0); // Max capacity: 3 kg — lighter, faster
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