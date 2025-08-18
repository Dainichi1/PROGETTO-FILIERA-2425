package unicam.progetto_filiera_springboot.strategy.validation;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
