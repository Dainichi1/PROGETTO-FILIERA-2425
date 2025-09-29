package unicam.filiera.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator per la ricarica dei fondi dell'Acquirente.
 */
@Component
public class FondiValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        // In questo caso validiamo un semplice Double (importo)
        return Double.class.isAssignableFrom(clazz) || double.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target == null) {
            errors.rejectValue("fondi", "error.fondi", "⚠ L'importo non può essere nullo");
            return;
        }

        double importo;
        try {
            importo = (Double) target;
        } catch (Exception e) {
            errors.rejectValue("fondi", "error.fondi", "⚠ L'importo deve essere un numero");
            return;
        }

        validaInterno(importo, errors, false);
    }

    /**
     * Metodo statico per validazione manuale in Service/Controller.
     * Lancia IllegalArgumentException in caso di errore.
     */
    public static void valida(double importo) {
        validaInterno(importo, null, true);
    }

    // Logica condivisa
    private static void validaInterno(double importo, Errors errors, boolean throwException) {
        if (importo <= 0) {
            String msg = "⚠ L'importo deve essere maggiore di zero";
            if (throwException) {
                throw new IllegalArgumentException(msg);
            } else if (errors != null) {
                errors.rejectValue("fondi", "error.fondi", msg);
            }
        }
    }
}
