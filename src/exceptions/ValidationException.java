package exceptions;

public class ValidationException extends DroneDeliveryException {
    public ValidationException(String message) {
        super(message);
    }
}