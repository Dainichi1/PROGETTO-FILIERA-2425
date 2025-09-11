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

    /**
     * Metodo statico per validazione manuale in Service.
     * Lancia IllegalArgumentException in caso di errore.
     */
    public static void valida(ProdottoDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("⚠ Il DTO non può essere null");
        }
        validaInterno(dto, null, true);
    }

    // Logica condivisa
    private static void validaInterno(ProdottoDto dto, Errors errors, boolean throwException) {
        boolean isCreazione = (dto.getId() == null);

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
