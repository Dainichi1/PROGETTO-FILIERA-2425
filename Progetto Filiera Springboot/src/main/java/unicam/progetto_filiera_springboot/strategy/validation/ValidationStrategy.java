package unicam.progetto_filiera_springboot.strategy.validation;

import jakarta.validation.ValidationException;

public interface ValidationStrategy<T> {
    void validate(T target) throws ValidationException;
}
