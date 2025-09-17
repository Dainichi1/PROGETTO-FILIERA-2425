package unicam.filiera.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import unicam.filiera.dto.AddToCartRequestDto;

@Component
public class CarrelloValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return AddToCartRequestDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AddToCartRequestDto dto = (AddToCartRequestDto) target;
        validaInterno(dto, errors, false);
    }

    /**
     * Validazione manuale (usata nei Service o Controller).
     * Lancia IllegalArgumentException in caso di errore.
     */
    public static void valida(AddToCartRequestDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("⚠ La richiesta non può essere null");
        }
        validaInterno(dto, null, true);
    }

    // ======================
    // Logica condivisa
    // ======================
    private static void validaInterno(AddToCartRequestDto dto, Errors errors, boolean throwException) {
        if (dto.getQuantita() <= 0) {
            if (throwException) throw new IllegalArgumentException("⚠ La quantità deve essere maggiore di 0");
            errors.rejectValue("quantita", "error.quantita", "La quantità deve essere maggiore di 0");
        }

        if (dto.getTipo() == null) {
            if (throwException) throw new IllegalArgumentException("⚠ Il tipo dell’item è obbligatorio");
            errors.rejectValue("tipo", "error.tipo", "Il tipo dell’item è obbligatorio");
        }

        if (dto.getId() == null) {
            if (throwException) throw new IllegalArgumentException("⚠ L’ID dell’item è obbligatorio");
            errors.rejectValue("id", "error.id", "L’ID dell’item è obbligatorio");
        }
    }
}
