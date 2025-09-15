package unicam.filiera.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import unicam.filiera.dto.PrenotazioneVisitaDto;

@Component
public class PrenotazioneVisitaValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return PrenotazioneVisitaDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PrenotazioneVisitaDto dto = (PrenotazioneVisitaDto) target;
        validaInterno(dto, errors, false);
    }

    /**
     * Metodo statico per validazione manuale in Service.
     * Lancia IllegalArgumentException in caso di errore.
     */
    public static void valida(PrenotazioneVisitaDto dto, boolean alreadyExists) {
        if (dto == null) {
            throw new IllegalArgumentException("⚠ Il DTO non può essere null");
        }
        validaInterno(dto, null, true, alreadyExists);
    }

    // Logica condivisa
    private static void validaInterno(PrenotazioneVisitaDto dto, Errors errors, boolean throwException) {
        validaInterno(dto, errors, throwException, false);
    }

    private static void validaInterno(PrenotazioneVisitaDto dto,
                                      Errors errors,
                                      boolean throwException,
                                      boolean alreadyExists) {

        if (dto.getIdVisita() == null) {
            reject("idVisita", "⚠ ID visita mancante", errors, throwException);
        }

        if (dto.getNumeroPersone() == null || dto.getNumeroPersone() < 1) {
            reject("numeroPersone", "⚠ Devi inserire almeno 1 persona", errors, throwException);
        }

        if (alreadyExists) {
            reject("idVisita", "⚠ Hai già prenotato questa visita", errors, throwException);
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
