package exceptions;

public class BatteryInsufficientException extends DroneDeliveryException {
    public BatteryInsufficientException(String message) {
        super(message);
    }
}