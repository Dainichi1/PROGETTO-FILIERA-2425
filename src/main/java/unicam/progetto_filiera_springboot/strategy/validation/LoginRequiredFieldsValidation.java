package unicam.progetto_filiera_springboot.strategy.validation;

import unicam.progetto_filiera_springboot.dto.auth.LoginDto;

public class LoginRequiredFieldsValidation implements ValidationStrategy<LoginDto> {
    @Override
    public void validate(LoginDto dto) {
        if (dto.getUsername() == null || dto.getUsername().isBlank())
            throw new ValidationException("Inserisci uno username.");
        if (dto.getPassword() == null || dto.getPassword().isBlank())
            throw new ValidationException("Inserisci una password.");
        if (dto.getRuolo() == null)
            throw new ValidationException("Seleziona un ruolo.");
    }
}
