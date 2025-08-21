package unicam.progetto_filiera_springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import unicam.progetto_filiera_springboot.application.dto.PacchettoForm;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;
import unicam.progetto_filiera_springboot.application.dto.PacchettoResponse;
import unicam.progetto_filiera_springboot.application.service.PacchettoService;
import unicam.progetto_filiera_springboot.application.service.ProdottoService;
import unicam.progetto_filiera_springboot.domain.model.Ruolo;
import unicam.progetto_filiera_springboot.repository.UtenteRepository;

import java.security.Principal;
import java.util.List;

@Controller
public class DistributoreController {

    private final ProdottoService prodottoService;
    private final PacchettoService pacchettoService;
    private final UtenteRepository utenteRepository;

    public DistributoreController(ProdottoService prodottoService,
                                  PacchettoService pacchettoService,
                                  UtenteRepository utenteRepository) {
        this.prodottoService = prodottoService;
        this.pacchettoService = pacchettoService;
        this.utenteRepository = utenteRepository;
    }

    @GetMapping("/distributore")
    public String home(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";

        var user = utenteRepository.findById(principal.getName()).orElse(null);
        if (user == null || user.getRuolo() != Ruolo.DISTRIBUTORE_TIPICITA) {
            return "redirect:/login";
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("ruolo", user.getRuolo());
        model.addAttribute("pacchettoForm", new PacchettoForm());

        // Prodotti pubblicati nel marketplace (scelta A = APPROVATO)
        List<ProdottoResponse> prodotti = prodottoService.listApprovati();
        model.addAttribute("prodottiMarketplace", prodotti);

        // Riepilogo pacchetti del distributore (richiesto “mostra sempre”)
        List<PacchettoResponse> pacchetti = pacchettoService.pacchettiDi(user.getUsername());
        model.addAttribute("pacchetti", pacchetti);

        return "distributore/index";
    }
}
