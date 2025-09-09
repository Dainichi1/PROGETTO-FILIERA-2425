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
import unicam.filiera.dto.ProdottoDto;
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
        return new ProdottoDto();
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

        if (prodottoDto.getCertificati() == null || prodottoDto.getCertificati().isEmpty()
                || prodottoDto.getCertificati().stream().allMatch(MultipartFile::isEmpty)) {
            bindingResult.rejectValue("certificati", "error.certificati", "Devi caricare almeno un certificato");
        }
        if (prodottoDto.getFoto() == null || prodottoDto.getFoto().isEmpty()
                || prodottoDto.getFoto().stream().allMatch(MultipartFile::isEmpty)) {
            bindingResult.rejectValue("foto", "error.foto", "Devi caricare almeno una foto");
        }

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

            redirectAttrs.addFlashAttribute("successMessage", "Prodotto inviato al Curatore con successo");
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
            prodottoService.eliminaProdottoById(id, username);
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
}
