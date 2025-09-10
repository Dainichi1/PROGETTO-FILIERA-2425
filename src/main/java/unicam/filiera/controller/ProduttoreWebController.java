package unicam.filiera.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.filiera.dto.ItemTipo;
import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.entity.ProdottoEntity; // <-- import
import unicam.filiera.model.Prodotto;
import unicam.filiera.service.ProdottoService;

import java.util.List;

@Controller
@RequestMapping("/produttore")
public class ProduttoreWebController {

    private static final Logger log = LoggerFactory.getLogger(ProduttoreWebController.class);

    private final ProdottoService prodottoService;

    @Autowired
    public ProduttoreWebController(ProdottoService prodottoService) {
        this.prodottoService = prodottoService;
    }

    @ModelAttribute("prodottoDto")
    public ProdottoDto prodottoDto() {
        ProdottoDto d = new ProdottoDto();
        d.setTipo(ItemTipo.PRODOTTO);
        return d;
    }

    @GetMapping("/dashboard")
    public String dashboardProduttore(Model model, Authentication authentication) {
        if (!model.containsAttribute("prodottoDto")) {
            model.addAttribute("prodottoDto", new ProdottoDto());
        }

        String username = (authentication != null) ? authentication.getName() : "produttore_demo";
        List<Prodotto> prodotti = prodottoService.getProdottiCreatiDa(username);
        model.addAttribute("prodotti", prodotti);

        model.addAttribute("showForm", false);
        return "dashboard/produttore";
    }

    @PostMapping("/crea")
    public String creaProdotto(
            @Valid @ModelAttribute("prodottoDto") ProdottoDto prodottoDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttrs,
            Model model
    ) {
        String username = (authentication != null) ? authentication.getName() : "produttore_demo";

        // Validazione centralizzata (file obbligatori SOLO in creazione)
        validaProdotto(prodottoDto, bindingResult);

        if (bindingResult.hasErrors()) {
            log.warn("Creazione prodotto fallita per errori di validazione");

            List<Prodotto> prodotti = prodottoService.getProdottiCreatiDa(username);
            model.addAttribute("prodotti", prodotti);

            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);
            return "dashboard/produttore";
        }

        try {
            prodottoService.creaProdotto(prodottoDto, username);
            redirectAttrs.addFlashAttribute("createSuccessMessage", "Prodotto inviato al Curatore con successo");
            return "redirect:/produttore/dashboard";

        } catch (Exception ex) {
            log.error("Errore nella creazione del prodotto", ex);

            List<Prodotto> prodotti = prodottoService.getProdottiCreatiDa(username);
            model.addAttribute("prodotti", prodotti);

            model.addAttribute("errorMessage", "Errore: " + ex.getMessage());
            model.addAttribute("showForm", true);
            return "dashboard/produttore";
        }
    }

    @DeleteMapping("/elimina/{id}")
    @ResponseBody
    public ResponseEntity<String> eliminaProdotto(@PathVariable Long id, Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "produttore_demo";
        try {
            // strada B: policy nel service specifico
            prodottoService.eliminaById(id, username);
            return ResponseEntity.ok("Prodotto eliminato con successo");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            log.error("Errore durante eliminazione prodotto", e);
            return ResponseEntity.status(500).body("Errore interno durante l'eliminazione");
        }
    }

    // === NUOVO: endpoint JSON per pre-popolare il form in modifica ===
    @GetMapping("/api/prodotto/{id}")
    @ResponseBody
    public ResponseEntity<?> getProdotto(@PathVariable Long id, Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "produttore_demo";

        return prodottoService.findEntityById(id)
                .map((ProdottoEntity entity) -> {
                    if (!entity.getCreatoDa().equals(username)) {
                        return ResponseEntity.status(403).body("Non autorizzato");
                    }
                    record ProdottoView(
                            Long id, String nome, String descrizione,
                            Integer quantita, Double prezzo, String indirizzo,
                            String stato, String commento,
                            java.util.List<String> certificati, java.util.List<String> foto
                    ) {}
                    var view = new ProdottoView(
                            entity.getId(),
                            entity.getNome(),
                            entity.getDescrizione(),
                            entity.getQuantita(),
                            entity.getPrezzo(),
                            entity.getIndirizzo(),
                            entity.getStato().name(),
                            entity.getCommento(),
                            (entity.getCertificati()==null || entity.getCertificati().isBlank())
                                    ? java.util.List.of()
                                    : java.util.List.of(entity.getCertificati().split(",")),
                            (entity.getFoto()==null || entity.getFoto().isBlank())
                                    ? java.util.List.of()
                                    : java.util.List.of(entity.getFoto().split(","))
                    );
                    return ResponseEntity.ok(view);
                })
                .orElseGet(() -> ResponseEntity.status(404).body("Prodotto non trovato"));
    }

    // --- Validazione centralizzata ---
    private void validaProdotto(ProdottoDto prodottoDto, BindingResult bindingResult) {
        // File obbligatori SOLO se id == null (creazione).
        boolean isCreazione = (prodottoDto.getId() == null);

        if (isCreazione) {
            if (prodottoDto.getCertificati() == null || prodottoDto.getCertificati().isEmpty()
                    || prodottoDto.getCertificati().stream().allMatch(MultipartFile::isEmpty)) {
                bindingResult.rejectValue("certificati", "error.certificati", "Devi caricare almeno un certificato");
            }
            if (prodottoDto.getFoto() == null || prodottoDto.getFoto().isEmpty()
                    || prodottoDto.getFoto().stream().allMatch(MultipartFile::isEmpty)) {
                bindingResult.rejectValue("foto", "error.foto", "Devi caricare almeno una foto");
            }
        }
    }
}
