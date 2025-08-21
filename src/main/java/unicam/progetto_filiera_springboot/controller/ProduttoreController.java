package unicam.progetto_filiera_springboot.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
