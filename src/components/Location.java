
// File: components/Location.java

package components;

public class Location {
    public static final double MIN_COORDINATE = -1000.0;
    public static final double MAX_COORDINATE = 1000.0;

    private double x;
    private double y;

    public Location(double x, double y) {
        validateCoordinates(x, y);
        this.x = x;
        this.y = y;
    }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }

    // Setters
    public void setX(double x) {
        validateCoordinate(x, "x");
        this.x = x;
    }

    public void setY(double y) {
        validateCoordinate(y, "y");
        this.y = y;
    }

    public double distanceTo(Location other) {
        if (other == null) {
            throw new IllegalArgumentException("Location cannot be null.");
        }
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static boolean isWithinBounds(double x, double y) {
        return Double.isFinite(x)
                && Double.isFinite(y)
                && x >= MIN_COORDINATE && x <= MAX_COORDINATE
                && y >= MIN_COORDINATE && y <= MAX_COORDINATE;
    }

    public static void validateCoordinates(double x, double y) {
        if (!isWithinBounds(x, y)) {
            throw new IllegalArgumentException("Invalid coordinates. Allowed range is "
                    + MIN_COORDINATE + " to " + MAX_COORDINATE + ".");
        }
    }

    public static void validateCoordinate(double value, String axis) {
        if (!Double.isFinite(value) || value < MIN_COORDINATE || value > MAX_COORDINATE) {
            throw new IllegalArgumentException("Invalid " + axis + " coordinate. Allowed range is "
                    + MIN_COORDINATE + " to " + MAX_COORDINATE + ".");
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Location)) {
            return false;
        }
        Location location = (Location) other;
        return Double.compare(location.x, x) == 0 && Double.compare(location.y, y) == 0;
    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(x);
        int result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}