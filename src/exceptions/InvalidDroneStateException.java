package exceptions;

public class InvalidDroneStateException extends DroneDeliveryException {
    public InvalidDroneStateException(String message) {
        super(message);
    }
}