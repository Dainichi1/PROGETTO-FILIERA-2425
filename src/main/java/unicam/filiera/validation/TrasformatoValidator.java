package unicam.filiera.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;
import unicam.filiera.dto.ProdottoTrasformatoDto;

@Component
public class TrasformatoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ProdottoTrasformatoDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ProdottoTrasformatoDto dto = (ProdottoTrasformatoDto) target;
        validaInterno(dto, errors, false);
    }

    public static void valida(ProdottoTrasformatoDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("⚠ Il DTO non può essere null");
        }
        validaInterno(dto, null, true);
    }

    private static void validaInterno(ProdottoTrasformatoDto dto, Errors errors, boolean throwException) {

        // certificati obbligatori SEMPRE
        if (dto.getCertificati() == null || dto.getCertificati().isEmpty()
                || dto.getCertificati().stream().allMatch(MultipartFile::isEmpty)) {
            if (throwException) throw new IllegalArgumentException("⚠ Devi caricare almeno un certificato");
            if (errors != null) {
                errors.rejectValue("certificati", "error.certificati", "Devi caricare almeno un certificato");
            }
        }

        if (dto.getFoto() == null || dto.getFoto().isEmpty()
                || dto.getFoto().stream().allMatch(MultipartFile::isEmpty)) {
            if (throwException) throw new IllegalArgumentException("⚠ Devi caricare almeno una foto");
            if (errors != null) {
                errors.rejectValue("foto", "error.foto", "Devi caricare almeno una foto");
            }
        }

        long fasiValide = (dto.getFasiProduzione() == null) ? 0 :
                dto.getFasiProduzione().stream()
                        .filter(f -> f != null
                                && f.getDescrizioneFase() != null && !f.getDescrizioneFase().isBlank()
                                && f.getProduttoreUsername() != null && !f.getProduttoreUsername().isBlank()
                                && f.getProdottoOrigineId() != null)
                        .count();

        if (fasiValide < 2) {
            if (throwException) throw new IllegalArgumentException("⚠ Devi inserire almeno 2 fasi di produzione");
            if (errors != null) {
                errors.rejectValue("fasiProduzione", "error.fasiProduzione", "Devi inserire almeno 2 fasi di produzione");
            }
        }
    }
}
