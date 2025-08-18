package unicam.progetto_filiera_springboot.strategy.validation;

import unicam.progetto_filiera_springboot.dto.auth.RegisterDto;

public class RegisterRequiredFieldsValidation implements ValidationStrategy<RegisterDto> {
    @Override
    public void validate(RegisterDto dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank())
            throw new ValidationException("Username obbligatorio");
        if (dto.getPassword() == null || dto.getPassword().isBlank())
            throw new ValidationException("Password obbligatoria");
        if (dto.getNome() == null || dto.getNome().isBlank())
            throw new ValidationException("Nome obbligatorio");
        if (dto.getCognome() == null || dto.getCognome().isBlank())
            throw new ValidationException("Cognome obbligatorio");
        if (dto.getRuolo() == null)
            throw new ValidationException("Ruolo obbligatorio");
    }
}
