package unicam.filiera.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import unicam.filiera.dto.PrenotazioneFieraDto;

@Component
public class PrenotazioneFieraValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return PrenotazioneFieraDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PrenotazioneFieraDto dto = (PrenotazioneFieraDto) target;
        validaInterno(dto, errors, false);
    }

    /**
     * Metodo statico per validazione manuale in Service.
     * Lancia IllegalArgumentException in caso di errore.
     */
    public static void valida(PrenotazioneFieraDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("⚠ Il DTO non può essere null");
        }
        validaInterno(dto, null, true);
    }

    // Logica condivisa
    private static void validaInterno(PrenotazioneFieraDto dto, Errors errors, boolean throwException) {
        if (dto.getIdFiera() == null) {
            reject("idFiera", "⚠ ID fiera mancante", errors, throwException);
        }

        if (dto.getNumeroPersone() == null || dto.getNumeroPersone() < 1) {
            reject("numeroPersone", "⚠ Devi inserire almeno 1 persona", errors, throwException);
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
