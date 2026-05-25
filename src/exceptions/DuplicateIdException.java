package exceptions;

public class DuplicateIdException extends DroneDeliveryException {
    public DuplicateIdException(String message) {
        super(message);
    }
}