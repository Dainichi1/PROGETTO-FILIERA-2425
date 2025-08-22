package unicam.progetto_filiera_springboot.controller.prodotto.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import java.math.BigDecimal;
import unicam.progetto_filiera_springboot.application.dto.ProdottoForm;

@Component
public class ProdottoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ProdottoForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ProdottoForm f = (ProdottoForm) target;

        // Regola integrativa rispetto a Bean Validation:
        // - scala decimali del prezzo ≤ 2 (es. 12.345 → errore)
        BigDecimal prezzo = f.getPrezzo();
        if (prezzo != null && prezzo.scale() > 2) {
            errors.rejectValue("prezzo", "prezzo.decimals", "Il prezzo può avere al massimo 2 decimali.");
        }

    }
}
