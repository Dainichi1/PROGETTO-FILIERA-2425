package unicam.progetto_filiera_springboot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.progetto_filiera_springboot.application.dto.ProdottoForm;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;
import unicam.progetto_filiera_springboot.application.service.ProdottoService;
import unicam.progetto_filiera_springboot.repository.UtenteRepository;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/produttore")
@RequiredArgsConstructor
public class ProduttoreController {

    private final ProdottoService prodottoService;
    private final UtenteRepository utenteRepository;

    @GetMapping
    public String index(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        String username = principal.getName();

        var utente = utenteRepository.findById(username).orElse(null);
        if (utente == null) return "redirect:/login";

        model.addAttribute("username", username);
        model.addAttribute("ruolo", utente.getRuolo());

        List<ProdottoResponse> prodotti = prodottoService.prodottiDi(username);
        model.addAttribute("prodotti", prodotti != null ? prodotti : Collections.emptyList());

        if (!model.containsAttribute("form"))        model.addAttribute("form", new ProdottoForm());
        if (!model.containsAttribute("hasFormErrors")) model.addAttribute("hasFormErrors", false);

        return "produttore/index";
    }

    // ==== ELIMINAZIONE ====

    @GetMapping("/prodotti/{id}/elimina")
    public String confermaElimina(@PathVariable Long id, Principal principal, Model model, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";
        String username = principal.getName();

        try {
            ProdottoResponse p = prodottoService.findByIdAndOwner(id, username);

            if (!prodottoService.isEliminabile(id, username)) {
                ra.addFlashAttribute("errorMsg",
                        "Puoi eliminare solo prodotti con stato \"In Attesa\" o \"Rifiutato\".");
                return "redirect:/produttore";
            }

            model.addAttribute("prodotto", p);
            return "produttore/confirm-delete";

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage() != null ? e.getMessage() : "Operazione non consentita.");
            return "redirect:/produttore";
        }
    }

    @PostMapping("/prodotti/{id}/elimina")
    public String elimina(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";
        String username = principal.getName();

        try {
            prodottoService.elimina(id, username);
            ra.addFlashAttribute("successMsg", "Prodotto eliminato con successo.");
        } catch (IllegalStateException ise) {
            ra.addFlashAttribute("errorMsg", ise.getMessage());
        } catch (IllegalArgumentException iae) {
            ra.addFlashAttribute("errorMsg", iae.getMessage() != null ? iae.getMessage() : "Operazione non consentita.");
        }
        return "redirect:/produttore";
    }
}
