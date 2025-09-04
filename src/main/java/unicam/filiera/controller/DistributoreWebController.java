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
import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.service.PacchettoService;
import unicam.filiera.service.ProdottoService;

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
        return new PacchettoDto();
    }

    @GetMapping("/dashboard")
    public String dashboardDistributore(Model model) {
        if (!model.containsAttribute("pacchettoDto")) {
            model.addAttribute("pacchettoDto", new PacchettoDto());
        }
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
        // Validazione manuale file
        if (pacchettoDto.getCertificati() == null || pacchettoDto.getCertificati().isEmpty()
                || pacchettoDto.getCertificati().stream().allMatch(MultipartFile::isEmpty)) {
            bindingResult.rejectValue("certificati", "error.certificati", "⚠ Devi caricare almeno un certificato");
        }
        if (pacchettoDto.getFoto() == null || pacchettoDto.getFoto().isEmpty()
                || pacchettoDto.getFoto().stream().allMatch(MultipartFile::isEmpty)) {
            bindingResult.rejectValue("foto", "error.foto", "⚠ Devi caricare almeno una foto");
        }

        // Validazione extra sui prodotti selezionati
        if (pacchettoDto.getProdottiSelezionati() == null || pacchettoDto.getProdottiSelezionati().size() < 2) {
            bindingResult.rejectValue("prodottiSelezionati", "error.prodottiSelezionati",
                    "⚠ Devi selezionare almeno 2 prodotti per creare un pacchetto");
        }

        if (bindingResult.hasErrors()) {
            log.warn("❌ Creazione pacchetto fallita per errori di validazione");
            model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);
            return "dashboard/distributore";
        }

        try {
            String username = (authentication != null) ? authentication.getName() : "distributore_demo";
            pacchettoService.creaPacchetto(pacchettoDto, username);

            redirectAttrs.addFlashAttribute("successMessage", "✅ Pacchetto inviato al Curatore con successo!");
            return "redirect:/distributore/dashboard";

        } catch (Exception ex) {
            log.error("⚠️ Errore nella creazione del pacchetto", ex);
            model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
            model.addAttribute("errorMessage", "Errore: " + ex.getMessage());
            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);
            return "dashboard/distributore";
        }
    }
}
