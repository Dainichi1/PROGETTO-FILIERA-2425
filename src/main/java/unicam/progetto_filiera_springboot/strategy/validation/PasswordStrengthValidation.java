package unicam.progetto_filiera_springboot.strategy.validation;

import unicam.progetto_filiera_springboot.dto.auth.RegisterDto;

public class PasswordStrengthValidation implements ValidationStrategy<RegisterDto> {
    @Override
    public void validate(RegisterDto dto) {
        String p = dto.getPassword();
        if (p.length() < 6)
            throw new ValidationException("Password troppo corta (min 6)");
    }
}
