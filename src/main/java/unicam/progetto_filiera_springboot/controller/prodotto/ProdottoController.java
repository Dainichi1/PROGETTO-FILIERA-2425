package unicam.progetto_filiera_springboot.controller.prodotto;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import unicam.progetto_filiera_springboot.application.dto.ProdottoForm;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;
import unicam.progetto_filiera_springboot.application.service.ProdottoService;
import unicam.progetto_filiera_springboot.domain.actor.Produttore;
import unicam.progetto_filiera_springboot.strategy.validation.ValidationException;

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
                                                          HttpSession session) {

        Object attore = session.getAttribute("attore");
        if (!(attore instanceof Produttore prod)) {
            return ResponseEntity.status(401).build(); // non loggato come Produttore
        }

        if (foto == null || foto.isEmpty())
            throw new ValidationException("Devi caricare almeno una foto.");
        if (certificati == null || certificati.isEmpty())
            throw new ValidationException("Devi caricare almeno un certificato.");

        ProdottoResponse resp = prodottoService.creaProdottoConFile(form, prod.getUsername(), foto, certificati);
        return ResponseEntity.ok(resp);
    }
}
