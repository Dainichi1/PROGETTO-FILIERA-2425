package unicam.filiera.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;
import unicam.filiera.dto.ProdottoDto;

@Component
public class ProdottoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ProdottoDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ProdottoDto dto = (ProdottoDto) target;
        validaInterno(dto, errors, false);
    }

    public static void valida(ProdottoDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("⚠ Il DTO non può essere null");
        }
        validaInterno(dto, null, true);
    }

    private static void validaInterno(ProdottoDto dto, Errors errors, boolean throwException) {
        boolean isCreazione = (dto.getId() == null);

        // Quantità > 0 obbligatoria in fase di creazione/aggiornamento
        if (dto.getQuantita() <= 0) {
            if (throwException) throw new IllegalArgumentException("⚠ La quantità deve essere maggiore di zero");
            errors.rejectValue("quantita", "error.quantita", "La quantità deve essere maggiore di zero");
        }

        // Certificati obbligatori solo in creazione
        if (isCreazione) {
            if (dto.getCertificati() == null || dto.getCertificati().isEmpty()
                    || dto.getCertificati().stream().allMatch(MultipartFile::isEmpty)) {
                if (throwException) throw new IllegalArgumentException("⚠ Devi caricare almeno un certificato");
                errors.rejectValue("certificati", "error.certificati", "Devi caricare almeno un certificato");
            }
            if (dto.getFoto() == null || dto.getFoto().isEmpty()
                    || dto.getFoto().stream().allMatch(MultipartFile::isEmpty)) {
                if (throwException) throw new IllegalArgumentException("⚠ Devi caricare almeno una foto");
                errors.rejectValue("foto", "error.foto", "Devi caricare almeno una foto");
            }
        }
    }
}
