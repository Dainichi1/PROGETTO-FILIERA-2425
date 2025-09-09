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
import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.model.ProdottoTrasformato;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.service.ProdottoService;
import unicam.filiera.service.ProdottoTrasformatoService;
import unicam.filiera.service.UtenteService;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/trasformatore")
public class TrasformatoreWebController {

    private static final Logger log = LoggerFactory.getLogger(TrasformatoreWebController.class);

    private final ProdottoTrasformatoService trasformatoService;
    private final ProdottoService prodottoService;
    private final UtenteService utenteService;

    @Autowired
    public TrasformatoreWebController(ProdottoTrasformatoService trasformatoService,
                                      ProdottoService prodottoService,
                                      UtenteService utenteService) {
        this.trasformatoService = trasformatoService;
        this.prodottoService = prodottoService;
        this.utenteService = utenteService;
    }

    @ModelAttribute("trasformatoDto")
    public ProdottoTrasformatoDto trasformatoDto() {
        return new ProdottoTrasformatoDto();
    }

    @GetMapping("/dashboard")
    public String dashboardTrasformatore(Model model, Authentication authentication) {
        if (!model.containsAttribute("trasformatoDto")) {
            model.addAttribute("trasformatoDto", new ProdottoTrasformatoDto());
        }

        String username = (authentication != null) ? authentication.getName() : "trasformatore_demo";

        // Carico SOLO i prodotti trasformati creati dal trasformatore loggato
        List<ProdottoTrasformato> trasformati = trasformatoService.getProdottiTrasformatiCreatiDa(username);
        model.addAttribute("trasformati", trasformati);

        // Prodotti e produttori approvati per il form
        model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
        model.addAttribute("produttori", utenteService.getProduttori());

        model.addAttribute("showForm", false);
        return "dashboard/trasformatore";
    }

    @GetMapping("/prodotti/{usernameProduttore}")
    @ResponseBody
    public List<Map<String, String>> getProdottiApprovatiByProduttore(@PathVariable String usernameProduttore) {
        return prodottoService.getProdottiApprovatiByProduttore(usernameProduttore)
                .stream()
                .map(p -> Map.of(
                        "id", String.valueOf(p.getId()),
                        "nome", p.getNome()
                ))
                .toList();
    }

    @PostMapping("/crea")
    public String creaProdottoTrasformato(
            @Valid @ModelAttribute("trasformatoDto") ProdottoTrasformatoDto trasformatoDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttrs,
            Model model
    ) {
        String username = (authentication != null) ? authentication.getName() : "trasformatore_demo";

        // Validazione manuale file
        if (trasformatoDto.getCertificati() == null || trasformatoDto.getCertificati().isEmpty()
                || trasformatoDto.getCertificati().stream().allMatch(MultipartFile::isEmpty)) {
            bindingResult.rejectValue("certificati", "error.certificati", "Devi caricare almeno un certificato");
        }
        if (trasformatoDto.getFoto() == null || trasformatoDto.getFoto().isEmpty()
                || trasformatoDto.getFoto().stream().allMatch(MultipartFile::isEmpty)) {
            bindingResult.rejectValue("foto", "error.foto", "Devi caricare almeno una foto");
        }
        if (trasformatoDto.getFasiProduzione() == null || trasformatoDto.getFasiProduzione().size() < 2) {
            bindingResult.rejectValue("fasiProduzione", "error.fasiProduzione",
                    "Devi inserire almeno 2 fasi di produzione");
        }

        if (bindingResult.hasErrors()) {
            log.warn("Creazione prodotto trasformato fallita per errori di validazione");

            List<ProdottoTrasformato> trasformati = trasformatoService.getProdottiTrasformatiCreatiDa(username);
            model.addAttribute("trasformati", trasformati);
            model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
            model.addAttribute("produttori", utenteService.getProduttori());

            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);
            return "dashboard/trasformatore";
        }

        try {
            trasformatoService.creaProdottoTrasformato(trasformatoDto, username);

            redirectAttrs.addFlashAttribute("successMessage", "Prodotto trasformato inviato al Curatore con successo");
            return "redirect:/trasformatore/dashboard";

        } catch (Exception ex) {
            log.error("Errore nella creazione del prodotto trasformato", ex);

            List<ProdottoTrasformato> trasformati = trasformatoService.getProdottiTrasformatiCreatiDa(username);
            model.addAttribute("trasformati", trasformati);
            model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
            model.addAttribute("produttori", utenteService.getProduttori());

            model.addAttribute("errorMessage", "Errore: " + ex.getMessage());
            model.addAttribute("showForm", true);
            return "dashboard/trasformatore";
        }
    }

    @DeleteMapping("/elimina/{id}")
    @ResponseBody
    public ResponseEntity<String> eliminaTrasformato(@PathVariable Long id, Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "trasformatore_demo";

        try {
            trasformatoService.eliminaProdottoTrasformatoById(id, username);
            return ResponseEntity.ok("Prodotto trasformato eliminato con successo");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            log.error("Errore durante eliminazione prodotto trasformato", e);
            return ResponseEntity.status(500).body("Errore interno durante l'eliminazione");
        }
    }
}
