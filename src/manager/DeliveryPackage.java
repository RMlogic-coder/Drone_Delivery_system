

// file: manager/DeliveryPackage.java

package manager;

import components.Location;
import java.util.Locale;

public class DeliveryPackage {
    private int packageId;
    private double weight;
    private String priority;        // "NORMAL" or "URGENT"
    private Location destination;
    private boolean delivered;

    public DeliveryPackage(int packageId, double weight,
                           String priority, Location destination) {
        if (packageId <= 0) {
            throw new IllegalArgumentException("Package ID must be positive.");
        }
        if (!Double.isFinite(weight) || weight <= 0) {
            throw new IllegalArgumentException("Package weight must be a positive number.");
        }
        if (priority == null || priority.trim().isEmpty()) {
            throw new IllegalArgumentException("Package priority cannot be null or empty.");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Delivery destination cannot be null.");
        }

        this.packageId   = packageId;
        this.weight      = weight;
        this.priority    = priority.trim().toUpperCase(Locale.ROOT);
        this.destination = destination;
        this.delivered   = false;
    }

    // Getters
    public int      getPackageId()   { return packageId; }
    public double   getWeight()      { return weight; }
    public String   getPriority()    { return priority; }
    public Location getDestination() { return destination; }
    public boolean  isDelivered()    { return delivered; }

    // Setter
    public void setDelivered(boolean delivered) { this.delivered = delivered; }

    @Override
    public String toString() {
        return "\nPackage ID  : " + packageId +
               "\nWeight      : " + weight + " kg" +
               "\nPriority    : " + priority +
               "\nDestination : " + destination +
               "\nDelivered   : " + delivered;
    }
}