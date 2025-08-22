package unicam.progetto_filiera_springboot.controller.pacchetto.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import unicam.progetto_filiera_springboot.application.dto.PacchettoForm;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Component
public class PacchettoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return PacchettoForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PacchettoForm f = (PacchettoForm) target;

        // Regola integrativa: scala decimali del prezzo totale ≤ 2
        BigDecimal p = f.getPrezzoTotale();
        if (p != null && p.scale() > 2) {
            errors.rejectValue("prezzoTotale", "prezzo.scale", "Max 2 decimali");
        }

        // Regole integrative sulla composizione del pacchetto:
        // - almeno 2 prodotti distinti
        // - id prodotti validi (> 0)
        List<Long> ids = f.getProdottiIds();
        if (ids != null) {
            long distinct = ids.stream().filter(Objects::nonNull).distinct().count();
            if (distinct < 2) {
                errors.rejectValue("prodottiIds", "prodotti.min", "Seleziona almeno 2 prodotti");
            }
            boolean hasInvalidId = ids.stream().filter(Objects::nonNull).anyMatch(id -> id <= 0);
            if (hasInvalidId) {
                errors.rejectValue("prodottiIds", "prodotti.id.invalid", "Sono presenti ID prodotto non validi");
            }
        }
    }
}
