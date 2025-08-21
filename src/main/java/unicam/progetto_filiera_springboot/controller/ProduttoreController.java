package unicam.progetto_filiera_springboot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

        // POPOLA SEMPRE LA LISTA (anche vuota)
        List<ProdottoResponse> prodotti = prodottoService.prodottiDi(username);
        if (prodotti == null) prodotti = Collections.emptyList();
        model.addAttribute("prodotti", prodotti);

        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ProdottoForm());
        }

        return "produttore/index";
    }

    @GetMapping("/prodotti/{id}/elimina")
    public String confermaElimina(@PathVariable Long id, Principal principal, Model model, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";
        String username = principal.getName();

        try {
            // recupero dati per mostrare nome ecc.
            ProdottoResponse p = prodottoService.findByIdAndOwner(id, username);

            if (!prodottoService.isEliminabile(id, username)) {
                ra.addFlashAttribute("errorMsg",
                        "Puoi eliminare solo prodotti con stato \"In Attesa\" o \"Rifiutato\".");
                return "redirect:/produttore";
            }

            model.addAttribute("prodotto", p);
            return "produttore/confirm-delete"; // pagina di conferma (no logica, solo grafica)

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage() != null ? e.getMessage() : "Operazione non consentita.");
            return "redirect:/produttore";
        }
    }

    /**
     * STEP 2: eliminazione definitiva
     */
    @PostMapping("/prodotti/{id}/elimina")
    public String elimina(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";
        String username = principal.getName();

        try {
            prodottoService.elimina(id, username);
            ra.addFlashAttribute("successMsg", "Prodotto eliminato con successo.");
        } catch (IllegalStateException ise) {
            // es. tentativo di eliminare APPROVATO
            ra.addFlashAttribute("errorMsg", ise.getMessage());
        } catch (IllegalArgumentException iae) {
            ra.addFlashAttribute("errorMsg", iae.getMessage() != null ? iae.getMessage() : "Operazione non consentita.");
        }
        return "redirect:/produttore";
    }
}
