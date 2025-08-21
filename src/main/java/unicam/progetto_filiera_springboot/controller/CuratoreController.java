package unicam.progetto_filiera_springboot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.progetto_filiera_springboot.application.service.PacchettoService;
import unicam.progetto_filiera_springboot.application.service.ProdottoService;
import unicam.progetto_filiera_springboot.domain.model.Ruolo;
import unicam.progetto_filiera_springboot.repository.UtenteRepository;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/curatore")
@RequiredArgsConstructor
public class CuratoreController {

    private final ProdottoService prodottoService;
    private final PacchettoService pacchettoService;
    private final UtenteRepository utenteRepository;

    @GetMapping
    public String home(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        var user = utenteRepository.findById(principal.getName()).orElse(null);
        if (user == null || user.getRuolo() != Ruolo.CURATORE) return "redirect:/login";

        model.addAttribute("username", user.getUsername());
        model.addAttribute("ruolo", user.getRuolo());

        // Prodotti e pacchetti in attesa
        model.addAttribute("inAttesa", prodottoService.listInAttesa());
        model.addAttribute("pacchettiInAttesa", pacchettoService.listInAttesa());

        return "curatore/index";
    }

    // APPROVA PRODOTTO
    @PostMapping("/prodotti/{id}/approve")
    public String approve(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        if (!isCuratore(principal)) return "redirect:/login";
        prodottoService.approve(id);
        ra.addFlashAttribute("ok", "Prodotto approvato e rimosso dalla lista in attesa.");
        return "redirect:/curatore";
    }

    // RIFIUTA PRODOTTO
    @PostMapping("/prodotti/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(name = "commento", required = false) String commento,
                         Principal principal,
                         RedirectAttributes ra) {
        if (!isCuratore(principal)) return "redirect:/login";

        prodottoService.reject(id, Optional.ofNullable(commento));
        ra.addFlashAttribute("ok",
                (commento == null || commento.isBlank())
                        ? "Prodotto rifiutato e restituito al produttore per modifiche."
                        : "Prodotto rifiutato con commento. Restituito al produttore per modifiche.");
        return "redirect:/curatore";
    }

    // APPROVA PACCHETTO
    @PostMapping("/pacchetti/{id}/approve")
    public String approvePacchetto(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        if (!isCuratore(principal)) return "redirect:/login";
        pacchettoService.approve(id);
        ra.addFlashAttribute("ok", "Pacchetto approvato e rimosso dalla lista in attesa.");
        return "redirect:/curatore";
    }

    // RIFIUTA PACCHETTO (firma uniformata con Optional<String>)
    @PostMapping("/pacchetti/{id}/reject")
    public String rejectPacchetto(@PathVariable Long id,
                                  @RequestParam(name = "commento", required = false) String commento,
                                  Principal principal,
                                  RedirectAttributes ra) {
        if (!isCuratore(principal)) return "redirect:/login";

        pacchettoService.reject(id, Optional.ofNullable(commento));
        ra.addFlashAttribute("ok",
                (commento == null || commento.isBlank())
                        ? "Pacchetto rifiutato e rimandato al distributore."
                        : "Pacchetto rifiutato con commento.");
        return "redirect:/curatore";
    }

    // ---- helper ----
    private boolean isCuratore(Principal principal) {
        if (principal == null) return false;
        return utenteRepository.findById(principal.getName())
                .map(u -> u.getRuolo() == Ruolo.CURATORE)
                .orElse(false);
    }
}
