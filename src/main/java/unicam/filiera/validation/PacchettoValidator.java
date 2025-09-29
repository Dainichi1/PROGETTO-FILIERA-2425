package unicam.filiera.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;
import unicam.filiera.dto.PacchettoDto;

@Component
public class PacchettoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return PacchettoDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PacchettoDto dto = (PacchettoDto) target;
        validaInterno(dto, errors, false);
    }

    /**
     * Metodo statico per validazione manuale in Service.
     * Lancia IllegalArgumentException in caso di errore.
     */
    public static void valida(PacchettoDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("⚠ Il DTO non può essere null");
        }
        validaInterno(dto, null, true);
    }

    // Metodo interno per riuso della logica tra Errors e Exception
    private static void validaInterno(PacchettoDto dto, Errors errors, boolean throwException) {
        boolean isCreazione = (dto.getId() == null);

        // Quantità > 0 obbligatoria in creazione/aggiornamento
        if (dto.getQuantita() <= 0) {
            if (throwException) throw new IllegalArgumentException("⚠ La quantità del pacchetto deve essere maggiore di zero");
            errors.rejectValue("quantita", "error.quantita", "La quantità del pacchetto deve essere maggiore di zero");
        }

        // Certificati e foto obbligatori solo in creazione
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

        // Deve contenere almeno 2 prodotti
        if (dto.getProdottiSelezionati() == null || dto.getProdottiSelezionati().size() < 2) {
            if (throwException) throw new IllegalArgumentException("⚠ Devi selezionare almeno 2 prodotti per creare il pacchetto");
            errors.rejectValue("prodottiSelezionati", "error.prodottiSelezionati",
                    "Devi selezionare almeno 2 prodotti per creare il pacchetto");
        }
    }
}
