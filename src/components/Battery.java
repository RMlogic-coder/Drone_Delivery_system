
// File: components/Battery.java

package components;

public class Battery {
    public static final double MAX_LEVEL = 100.0;

    private double level;

    public Battery(double level) {
        setLevel(level);
    }

    // Getters
    public double getLevel() { return level; }

    // Setters
    public synchronized void setLevel(double level) {
        if (!Double.isFinite(level)) {
            throw new IllegalArgumentException("Battery level must be numeric.");
        }
        if (level > MAX_LEVEL) level = MAX_LEVEL;
        if (level < 0) level = 0;
        this.level = level;
    }

    // Drain battery, floor at 0
    public synchronized void drainBattery(double amount) {
        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Battery drain amount must be a non-negative number.");
        }
        level -= amount;
        if (level < 0) level = 0;
    }

    // Check if enough charge available  ,,
    public synchronized boolean hasSufficientCharge(double required) {
        if (!Double.isFinite(required) || required < 0) {
            throw new IllegalArgumentException("Required battery must be a non-negative number.");
        }
        return level >= required;
    }

    // full charge
    public synchronized void chargeBattery() {
        level = MAX_LEVEL;
        System.out.println("Battery fully charged to 100%.");
    }
}
