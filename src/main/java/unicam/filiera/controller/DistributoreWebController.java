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
import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.service.PacchettoService;
import unicam.filiera.service.ProdottoService;

import java.util.List;

@Controller
@RequestMapping("/distributore")
public class DistributoreWebController {

    private static final Logger log = LoggerFactory.getLogger(DistributoreWebController.class);

    private final PacchettoService pacchettoService;
    private final ProdottoService prodottoService;

    @Autowired
    public DistributoreWebController(PacchettoService pacchettoService,
                                     ProdottoService prodottoService) {
        this.pacchettoService = pacchettoService;
        this.prodottoService = prodottoService;
    }

    @ModelAttribute("pacchettoDto")
    public PacchettoDto pacchettoDto() {
        PacchettoDto d = new PacchettoDto();
        d.setTipo(ItemTipo.PACCHETTO);
        return d;
    }

    @GetMapping("/dashboard")
    public String dashboardDistributore(Model model, Authentication authentication) {
        if (!model.containsAttribute("pacchettoDto")) {
            model.addAttribute("pacchettoDto", new PacchettoDto());
        }

        String username = (authentication != null) ? authentication.getName() : "distributore_demo";

        model.addAttribute("pacchetti", pacchettoService.getPacchettiViewByCreatore(username));
        model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
        model.addAttribute("showForm", false);

        return "dashboard/distributore";
    }

    @PostMapping("/crea")
    public String creaPacchetto(
            @Valid @ModelAttribute("pacchettoDto") PacchettoDto pacchettoDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttrs,
            Model model
    ) {
        String username = (authentication != null) ? authentication.getName() : "distributore_demo";

        validaPacchetto(pacchettoDto, bindingResult);

        if (bindingResult.hasErrors()) {
            log.warn("Creazione pacchetto fallita per errori di validazione");
            model.addAttribute("pacchetti", pacchettoService.getPacchettiCreatiDa(username));
            model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);
            return "dashboard/distributore";
        }

        try {
            pacchettoService.creaPacchetto(pacchettoDto, username);
            redirectAttrs.addFlashAttribute("createSuccessMessage", "Pacchetto inviato al Curatore con successo");
            return "redirect:/distributore/dashboard";
        } catch (Exception ex) {
            log.error("Errore nella creazione del pacchetto", ex);
            model.addAttribute("pacchetti", pacchettoService.getPacchettiCreatiDa(username));
            model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
            model.addAttribute("errorMessage", "Errore: " + ex.getMessage());
            model.addAttribute("showForm", true);
            return "dashboard/distributore";
        }
    }

    @DeleteMapping("/elimina/{id}")
    @ResponseBody
    public ResponseEntity<String> eliminaPacchetto(@PathVariable Long id, Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "distributore_demo";
        try {
            pacchettoService.eliminaById(id, username);
            return ResponseEntity.ok("Pacchetto eliminato con successo");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            log.error("Errore durante eliminazione pacchetto", e);
            return ResponseEntity.status(500).body("Errore interno durante l'eliminazione");
        }
    }

    @GetMapping("/api/pacchetto/{id}")
    @ResponseBody
    public ResponseEntity<?> getPacchetto(@PathVariable Long id, Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "distributore_demo";

        return pacchettoService.findEntityById(id)
                .map(entity -> {
                    if (!entity.getCreatoDa().equals(username)) {
                        return ResponseEntity.status(403).body("Non autorizzato");
                    }
                    record PacchettoView(
                            Long id, String nome, String descrizione,
                            Integer quantita, Double prezzo, String indirizzo,
                            String stato, String commento,
                            java.util.List<Long> prodottiSelezionati,
                            java.util.List<String> certificati,
                            java.util.List<String> foto
                    ) {}
                    var view = new PacchettoView(
                            entity.getId(),
                            entity.getNome(),
                            entity.getDescrizione(),
                            entity.getQuantita(),
                            entity.getPrezzo(),
                            entity.getIndirizzo(),
                            entity.getStato().name(),
                            entity.getCommento(),
                            entity.getProdotti() == null ? java.util.List.of()
                                    : entity.getProdotti().stream().map(p -> p.getId()).toList(),
                            (entity.getCertificati() == null || entity.getCertificati().isBlank())
                                    ? java.util.List.of()
                                    : java.util.List.of(entity.getCertificati().split(",")),
                            (entity.getFoto() == null || entity.getFoto().isBlank())
                                    ? java.util.List.of()
                                    : java.util.List.of(entity.getFoto().split(","))
                    );
                    return ResponseEntity.ok(view);
                })
                .orElseGet(() -> ResponseEntity.status(404).body("Pacchetto non trovato"));
    }

    // --- Validazione centralizzata ---
    private void validaPacchetto(PacchettoDto pacchettoDto, BindingResult bindingResult) {
        if (pacchettoDto.getCertificati() == null || pacchettoDto.getCertificati().isEmpty()
                || pacchettoDto.getCertificati().stream().allMatch(MultipartFile::isEmpty)) {
            bindingResult.rejectValue("certificati", "error.certificati", "Devi caricare almeno un certificato");
        }
        if (pacchettoDto.getFoto() == null || pacchettoDto.getFoto().isEmpty()
                || pacchettoDto.getFoto().stream().allMatch(MultipartFile::isEmpty)) {
            bindingResult.rejectValue("foto", "error.foto", "Devi caricare almeno una foto");
        }
        if (pacchettoDto.getProdottiSelezionati() == null || pacchettoDto.getProdottiSelezionati().size() < 2) {
            bindingResult.rejectValue("prodottiSelezionati", "error.prodottiSelezionati",
                    "Devi selezionare almeno 2 prodotti per creare il pacchetto");
        }
    }
}
