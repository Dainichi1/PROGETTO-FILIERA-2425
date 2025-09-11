package unicam.filiera.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import unicam.filiera.dto.PostSocialDto;

@Component
public class PostValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return PostSocialDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PostSocialDto dto = (PostSocialDto) target;

        if (dto.getTitolo() == null || dto.getTitolo().isBlank()) {
            errors.rejectValue("titolo", "error.titolo", "⚠ Il titolo è obbligatorio");
        } else if (dto.getTitolo().length() > 100) {
            errors.rejectValue("titolo", "error.titolo", "⚠ Il titolo non può superare i 100 caratteri");
        }

        if (dto.getTesto() == null || dto.getTesto().isBlank()) {
            errors.rejectValue("testo", "error.testo", "⚠ Il testo è obbligatorio");
        } else if (dto.getTesto().length() > 1000) {
            errors.rejectValue("testo", "error.testo", "⚠ Il testo non può superare i 1000 caratteri");
        }
    }

    /**
     * Metodo statico per validazione manuale, usato nei Service.
     * Lancia IllegalArgumentException in caso di errore.
     */
    public static void valida(PostSocialDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Il DTO non può essere null");
        }
        if (dto.getTitolo() == null || dto.getTitolo().isBlank()) {
            throw new IllegalArgumentException("⚠ Il titolo è obbligatorio");
        }
        if (dto.getTitolo().length() > 100) {
            throw new IllegalArgumentException("⚠ Il titolo non può superare i 100 caratteri");
        }
        if (dto.getTesto() == null || dto.getTesto().isBlank()) {
            throw new IllegalArgumentException("⚠ Il testo è obbligatorio");
        }
        if (dto.getTesto().length() > 1000) {
            throw new IllegalArgumentException("⚠ Il testo non può superare i 1000 caratteri");
        }
    }
}
