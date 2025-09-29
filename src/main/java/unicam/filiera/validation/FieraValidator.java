package unicam.filiera.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import unicam.filiera.dto.FieraDto;

import java.time.LocalDate;

@Component
public class FieraValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return FieraDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        FieraDto dto = (FieraDto) target;
        validaInterno(dto, errors, false);
    }

    /**
     * Metodo statico per validazione manuale in Service.
     * Lancia IllegalArgumentException in caso di errore.
     */
    public static void valida(FieraDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("⚠ Il DTO non può essere null");
        }
        validaInterno(dto, null, true);
    }

    // Logica condivisa
    private static void validaInterno(FieraDto dto, Errors errors, boolean throwException) {
        // Controllo date
        LocalDate inizio = dto.getDataInizio();
        LocalDate fine = dto.getDataFine();
        if (inizio == null || fine == null || !fine.isAfter(inizio)) {
            reject("dataFine", "⚠ La data di fine deve essere successiva a quella di inizio", errors, throwException);
        }

        // Controllo indirizzo
        if (dto.getIndirizzo() == null || dto.getIndirizzo().isBlank()) {
            reject("indirizzo", "⚠ Indirizzo mancante o vuoto", errors, throwException);
        }

        // Controllo prezzo
        if (dto.getPrezzo() == null) {
            reject("prezzo", "⚠ Il prezzo è obbligatorio", errors, throwException);
        } else if (dto.getPrezzo() < 0) {
            reject("prezzo", "⚠ Il prezzo non può essere negativo", errors, throwException);
        }
    }

    private static void reject(String field, String msg, Errors errors, boolean throwException) {
        if (throwException) {
            throw new IllegalArgumentException(msg);
        } else if (errors != null) {
            errors.rejectValue(field, "error." + field, msg);
        }
    }
}
