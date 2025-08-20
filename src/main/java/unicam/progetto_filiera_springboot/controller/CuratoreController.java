package unicam.progetto_filiera_springboot.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;
import unicam.progetto_filiera_springboot.application.service.ProdottoService;
import unicam.progetto_filiera_springboot.domain.actor.Curatore;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/curatore")
@RequiredArgsConstructor
public class CuratoreController {

    private final ProdottoService prodottoService;

    /**
     * Home del Curatore:
     * - accesso consentito solo se in sessione c’è un Curatore
     * - mostra la lista dei prodotti con stato IN_ATTESA
     */
    @GetMapping
    public String home(HttpSession session, Model model) {
        Object attore = session.getAttribute("attore");

        if (!(attore instanceof Curatore c)) {
            return "redirect:/login";
        }

        model.addAttribute("username", c.getUsername());
        model.addAttribute("ruolo", c.getRuolo());

        // Carico i prodotti con stato IN_ATTESA
        List<ProdottoResponse> inAttesa = prodottoService.listInAttesa();
        model.addAttribute("inAttesa", inAttesa);

        return "curatore/index";
    }

    /**
     * Approva un prodotto:
     * - imposta stato APPROVATO
     * - rimuove dalla lista IN_ATTESA
     */
    @PostMapping("/prodotti/{id}/approve")
    public String approve(@PathVariable Long id,
                          HttpSession session,
                          RedirectAttributes ra) {
        if (!(session.getAttribute("attore") instanceof Curatore)) {
            return "redirect:/login";
        }

        prodottoService.approve(id);
        ra.addFlashAttribute("ok", "Prodotto approvato e rimosso dalla lista in attesa.");
        return "redirect:/curatore";
    }

    /**
     * Rifiuta un prodotto:
     * - imposta stato RIFIUTATO
     * - commento opzionale
     */
    @PostMapping("/prodotti/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(name = "commento", required = false) String commento,
                         HttpSession session,
                         RedirectAttributes ra) {
        if (!(session.getAttribute("attore") instanceof Curatore)) {
            return "redirect:/login";
        }

        prodottoService.reject(id, Optional.ofNullable(commento));
        ra.addFlashAttribute("ok",
                (commento == null || commento.isBlank())
                        ? "Prodotto rifiutato e restituito al produttore per modifiche."
                        : "Prodotto rifiutato con commento. Restituito al produttore per modifiche.");
        return "redirect:/curatore";
    }
}
