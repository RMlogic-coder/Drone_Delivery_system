package exceptions;

public class DroneUnavailableException extends DroneDeliveryException {
    public DroneUnavailableException(String message) {
        super(message);
    }
}