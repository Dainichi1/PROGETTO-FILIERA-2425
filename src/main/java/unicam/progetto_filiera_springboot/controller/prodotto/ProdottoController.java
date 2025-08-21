package unicam.progetto_filiera_springboot.controller.prodotto;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import unicam.progetto_filiera_springboot.application.dto.ProdottoForm;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;
import unicam.progetto_filiera_springboot.application.service.ProdottoService;
import unicam.progetto_filiera_springboot.strategy.validation.ValidationException;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/prodotti")
public class ProdottoController {

    private final ProdottoService prodottoService;

    public ProdottoController(ProdottoService prodottoService) {
        this.prodottoService = prodottoService;
    }

    /**
     * Crea un prodotto (stato IN_ATTESA) con upload obbligatorio di foto e certificati.
     * Consuma multipart/form-data per ricevere sia i campi del form sia i file.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProdottoResponse> creaMultipart(@Valid @ModelAttribute ProdottoForm form,
                                                          @RequestParam("foto") List<MultipartFile> foto,
                                                          @RequestParam("certificati") List<MultipartFile> certificati,
                                                          Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).build(); // non autenticato
        }
        String username = principal.getName();

        if (foto == null || foto.isEmpty()) {
            throw new ValidationException("Devi caricare almeno una foto.");
        }
        if (certificati == null || certificati.isEmpty()) {
            throw new ValidationException("Devi caricare almeno un certificato.");
        }

        ProdottoResponse resp = prodottoService.creaProdottoConFile(form, username, foto, certificati);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()           // /api/prodotti
                .path("/{id}")                  // /{id}
                .buildAndExpand(resp.getId())
                .toUri();

        return ResponseEntity.created(location).body(resp); // 201 + Location
    }
}
