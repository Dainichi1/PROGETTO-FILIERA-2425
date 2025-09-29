package unicam.filiera.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import unicam.filiera.dto.DatiAcquistoDto;

@Component
public class AcquistoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return DatiAcquistoDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        DatiAcquistoDto dto = (DatiAcquistoDto) target;
        validaInterno(dto, errors, false);
    }

    /**
     * Validazione manuale (usata nei Service o Controller).
     * Lancia IllegalArgumentException in caso di errore.
     */
    public static void valida(DatiAcquistoDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("⚠ I dati dell’acquisto non possono essere null");
        }
        validaInterno(dto, null, true);
    }

    // ======================
    // Logica condivisa
    // ======================
    private static void validaInterno(DatiAcquistoDto dto, Errors errors, boolean throwException) {
        if (dto.getUsernameAcquirente() == null || dto.getUsernameAcquirente().isBlank()) {
            reject("usernameAcquirente", "⚠ Username acquirente obbligatorio", errors, throwException);
        }

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            reject("items", "⚠ L’acquisto deve contenere almeno un item", errors, throwException);
            return; // inutile continuare senza items
        }

        // Se il totale non è stato calcolato dal frontend, lo ricalcolo dagli items
        double totaleCalcolato = dto.getItems().stream()
                .mapToDouble(i -> i.getTotale())
                .sum();

        if (dto.getTotaleAcquisto() <= 0 && totaleCalcolato > 0) {
            dto.setTotaleAcquisto(totaleCalcolato);
        }

        if (dto.getTotaleAcquisto() <= 0) {
            reject("totaleAcquisto", "⚠ Il totale deve essere maggiore di zero", errors, throwException);
        }

        if (dto.getTipoMetodoPagamento() == null) {
            reject("tipoMetodoPagamento", "⚠ Devi selezionare un metodo di pagamento", errors, throwException);
        }
    }

    private static void reject(String field, String message, Errors errors, boolean throwException) {
        if (throwException) throw new IllegalArgumentException(message);
        if (errors != null) errors.rejectValue(field, "error." + field, message);
    }
}
