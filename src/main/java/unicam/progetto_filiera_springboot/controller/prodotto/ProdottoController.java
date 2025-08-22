package unicam.progetto_filiera_springboot.controller.prodotto;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import unicam.progetto_filiera_springboot.application.dto.ProdottoForm;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;
import unicam.progetto_filiera_springboot.application.service.ProdottoService;
import unicam.progetto_filiera_springboot.controller.prodotto.validation.ProdottoValidator;
import unicam.progetto_filiera_springboot.strategy.validation.ValidationException;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/prodotti")
public class ProdottoController {

    private final ProdottoService prodottoService;
    private final ProdottoValidator prodottoValidator; // opzionale ma consigliato

    public ProdottoController(ProdottoService prodottoService,
                              ProdottoValidator prodottoValidator) {
        this.prodottoService = prodottoService;
        this.prodottoValidator = prodottoValidator;
    }

    // valida anche il form via validator custom (coerente con MVC)
    @InitBinder("prodottoForm")
    void initBinder(WebDataBinder binder) {
        binder.addValidators(prodottoValidator);
    }

    /**
     * Crea un prodotto (stato IN_ATTESA) con upload obbligatorio di foto e certificati.
     * Consuma multipart/form-data per ricevere sia i campi del form sia i file.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProdottoResponse> creaMultipart(@Valid @ModelAttribute("prodottoForm") ProdottoForm prodottoForm,
                                                          @RequestParam("foto") List<MultipartFile> foto,
                                                          @RequestParam("certificati") List<MultipartFile> certificati,
                                                          Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).build(); // non autenticato
        }

        String username = principal.getName();
        validateFiles(foto, certificati); // controlla anche file "vuoti"

        ProdottoResponse resp = prodottoService.creaProdottoConFile(prodottoForm, username, foto, certificati);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(resp.getId())
                .toUri();

        return ResponseEntity.created(location).body(resp);
    }

    /**
     * Verifica lato server: almeno una foto e un certificato NON vuoti.
     */
    private void validateFiles(List<MultipartFile> foto, List<MultipartFile> certificati) {
        if (allEmpty(foto)) {
            throw new ValidationException("Devi caricare almeno una foto.");
        }
        if (allEmpty(certificati)) {
            throw new ValidationException("Devi caricare almeno un certificato.");
        }
    }

    private static boolean allEmpty(List<MultipartFile> files) {
        return files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty);
    }
}
