package unicam.progetto_filiera_springboot.controller.pacchetto;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import unicam.progetto_filiera_springboot.application.dto.PacchettoForm;
import unicam.progetto_filiera_springboot.application.dto.PacchettoResponse;
import unicam.progetto_filiera_springboot.application.service.PacchettoService;
import unicam.progetto_filiera_springboot.controller.pacchetto.validation.PacchettoValidator;
import unicam.progetto_filiera_springboot.strategy.validation.ValidationException;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/pacchetti")
public class PacchettoController {

    private final PacchettoService pacchettoService;
    private final PacchettoValidator pacchettoValidator;

    public PacchettoController(PacchettoService pacchettoService,
                               PacchettoValidator pacchettoValidator) {
        this.pacchettoService = pacchettoService;
        this.pacchettoValidator = pacchettoValidator;
    }

    // Collega il validator custom al model attribute "pacchettoForm"
    @InitBinder("pacchettoForm")
    void initBinder(WebDataBinder binder) {
        binder.addValidators(pacchettoValidator);
    }

    /**
     * Crea un pacchetto (stato IN_ATTESA) con upload obbligatorio di foto e certificati.
     * Consuma multipart/form-data per ricevere sia i campi del form sia i file.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PacchettoResponse> creaPacchetto(@Valid @ModelAttribute("pacchettoForm") PacchettoForm pacchettoForm,
                                                           @RequestParam("foto") List<MultipartFile> foto,
                                                           @RequestParam("certificati") List<MultipartFile> certificati,
                                                           Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build(); // non autenticato
        }

        // Verifica lato REST: almeno una foto e un certificato NON vuoti (coerente con ProdottoController)
        if (allEmpty(foto)) {
            throw new ValidationException("Devi caricare almeno una foto.");
        }
        if (allEmpty(certificati)) {
            throw new ValidationException("Devi caricare almeno un certificato.");
        }

        PacchettoResponse resp = pacchettoService.creaPacchettoConFile(
                pacchettoForm,
                principal.getName(),
                foto,
                certificati
        );

        // Location: /api/pacchetti/{id}
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()      // /api/pacchetti
                .path("/{id}")
                .buildAndExpand(resp.getId())
                .toUri();

        return ResponseEntity.created(location).body(resp); // 201 + Location
    }

    @GetMapping("/miei")
    public ResponseEntity<List<PacchettoResponse>> mieiPacchetti(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(pacchettoService.pacchettiDi(principal.getName()));
    }

    // ---- helpers ----
    private static boolean allEmpty(List<MultipartFile> files) {
        return files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty);
    }
}
