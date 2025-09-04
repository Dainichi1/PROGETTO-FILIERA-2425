package unicam.filiera.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.service.PacchettoService;
import unicam.filiera.service.ProdottoService;

@Controller
@RequestMapping("/distributore")
public class DistributoreWebController {

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
            Model model,
            Authentication authentication
    ) {
        // Validazione manuale per file
        if (pacchettoDto.getCertificati() == null || pacchettoDto.getCertificati().isEmpty()
                || pacchettoDto.getCertificati().stream().allMatch(MultipartFile::isEmpty)) {
            bindingResult.rejectValue("certificati", "error.certificati", "⚠ Devi caricare almeno un certificato");
        }
        if (pacchettoDto.getFoto() == null || pacchettoDto.getFoto().isEmpty()
                || pacchettoDto.getFoto().stream().allMatch(MultipartFile::isEmpty)) {
            bindingResult.rejectValue("foto", "error.foto", "⚠ Devi caricare almeno una foto");
        }

        // Validazione per prodotti selezionati (min 2)
        if (pacchettoDto.getProdottiSelezionati() == null || pacchettoDto.getProdottiSelezionati().size() < 2) {
            bindingResult.rejectValue("prodottiSelezionati", "error.prodottiSelezionati",
                    "⚠ Devi selezionare almeno 2 prodotti per creare un pacchetto");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true); // come per il Produttore
            return "dashboard/distributore";
        }

        try {
            String username = (authentication != null) ? authentication.getName() : "distributore_demo";
            pacchettoService.creaPacchetto(pacchettoDto, username);

            model.addAttribute("successMessage", "Pacchetto inviato al Curatore con successo!");
            model.addAttribute("pacchettoDto", new PacchettoDto());
            model.addAttribute("showForm", false);

        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Errore: " + ex.getMessage());
            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);
        }

        model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
        return "dashboard/distributore";
    }
}
