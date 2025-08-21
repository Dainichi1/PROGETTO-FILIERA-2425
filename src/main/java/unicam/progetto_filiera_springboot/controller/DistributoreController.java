package unicam.progetto_filiera_springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
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

        // Prodotti approvati
        List<ProdottoResponse> prodotti = prodottoService.listApprovati();
        model.addAttribute("prodottiMarketplace", prodotti);

        // Pacchetti del distributore
        List<PacchettoResponse> pacchetti = pacchettoService.pacchettiDi(user.getUsername());
        model.addAttribute("pacchetti", pacchetti);

        return "distributore/index";
    }

    @GetMapping("/distributore/pacchetti/{id}/elimina")
    public String confermaElimina(@PathVariable Long id, Principal principal, Model model, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";
        String username = principal.getName();

        try {
            PacchettoResponse p = pacchettoService.findByIdAndOwner(id, username);

            if (!pacchettoService.isEliminabile(id, username)) {
                ra.addFlashAttribute("errorMsg",
                        "Puoi eliminare solo pacchetti con stato \"In Attesa\" o \"Rifiutato\".");
                return "redirect:/distributore";
            }

            model.addAttribute("pacchetto", p);
            return "distributore/confirm-delete"; // view di conferma

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg",
                    e.getMessage() != null ? e.getMessage() : "Operazione non consentita.");
            return "redirect:/distributore";
        }
    }

    @PostMapping("/distributore/pacchetti/{id}/elimina")
    public String elimina(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";
        String username = principal.getName();

        try {
            pacchettoService.elimina(id, username);
            ra.addFlashAttribute("successMsg", "Pacchetto eliminato con successo.");
        } catch (IllegalStateException ise) {
            ra.addFlashAttribute("errorMsg", ise.getMessage());
        } catch (IllegalArgumentException iae) {
            ra.addFlashAttribute("errorMsg",
                    iae.getMessage() != null ? iae.getMessage() : "Operazione non consentita.");
        }
        return "redirect:/distributore";
    }
}
