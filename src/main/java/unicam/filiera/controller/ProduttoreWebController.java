package unicam.filiera.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.service.ProdottoService;

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
    public String dashboardProduttore(Model model) {
        if (!model.containsAttribute("prodottoDto")) {
            model.addAttribute("prodottoDto", new ProdottoDto());
        }
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
        // Validazione manuale file
        if (prodottoDto.getCertificati() == null || prodottoDto.getCertificati().isEmpty()
                || prodottoDto.getCertificati().stream().allMatch(MultipartFile::isEmpty)) {
            bindingResult.rejectValue("certificati", "error.certificati", "⚠ Devi caricare almeno un certificato");
        }
        if (prodottoDto.getFoto() == null || prodottoDto.getFoto().isEmpty()
                || prodottoDto.getFoto().stream().allMatch(MultipartFile::isEmpty)) {
            bindingResult.rejectValue("foto", "error.foto", "⚠ Devi caricare almeno una foto");
        }

        if (bindingResult.hasErrors()) {
            log.warn("❌ Creazione prodotto fallita per errori di validazione");
            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);
            return "dashboard/produttore";
        }

        try {
            String username = (authentication != null) ? authentication.getName() : "produttore_demo";
            prodottoService.creaProdotto(prodottoDto, username);

            redirectAttrs.addFlashAttribute("successMessage", "✅ Prodotto inviato al Curatore con successo!");
            return "redirect:/produttore/dashboard";

        } catch (Exception ex) {
            log.error("⚠️ Errore nella creazione del prodotto", ex);
            model.addAttribute("errorMessage", "Errore: " + ex.getMessage());
            model.addAttribute("showForm", true);
            return "dashboard/produttore";
        }
    }
}
