package unicam.filiera.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import unicam.filiera.dto.VisitaInvitoDto;

import java.time.LocalDate;

@Component
public class VisitaInvitoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return VisitaInvitoDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        VisitaInvitoDto dto = (VisitaInvitoDto) target;
        validaInterno(dto, errors, false);
    }

    /**
     * Metodo statico per validazione manuale in Service.
     * Lancia IllegalArgumentException in caso di errore.
     */
    public static void valida(VisitaInvitoDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("⚠ Il DTO non può essere null");
        }
        validaInterno(dto, null, true);
    }

    // Logica condivisa
    private static void validaInterno(VisitaInvitoDto dto, Errors errors, boolean throwException) {
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

        // Controllo destinatari
        if (dto.getDestinatari() == null || dto.getDestinatari().isEmpty()) {
            reject("destinatari", "⚠ Devi selezionare almeno un destinatario", errors, throwException);
        } else if (dto.getDestinatari().stream().anyMatch(d -> d == null || d.isBlank())) {
            reject("destinatari", "⚠ Uno dei destinatari è vuoto o non valido", errors, throwException);
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
