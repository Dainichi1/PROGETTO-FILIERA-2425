package unicam.progetto_filiera_springboot.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import unicam.progetto_filiera_springboot.application.dto.ProdottoForm;
import unicam.progetto_filiera_springboot.application.dto.ProdottoResponse;
import unicam.progetto_filiera_springboot.application.service.ProdottoService;
import unicam.progetto_filiera_springboot.domain.actor.Produttore;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/produttore")
@RequiredArgsConstructor
public class ProduttoreController {

    private final ProdottoService prodottoService;

    @GetMapping
    public String index(HttpSession session, Model model) {
        Object attore = session.getAttribute("attore");

        if (!(attore instanceof Produttore p)) {
            return "redirect:/login";
        }

        model.addAttribute("username", p.getUsername());
        model.addAttribute("ruolo", p.getRuolo());

        // POPOLA SEMPRE LA LISTA (anche vuota)
        List<ProdottoResponse> prodotti = prodottoService.prodottiDi(p.getUsername());
        if (prodotti == null) prodotti = Collections.emptyList();
        model.addAttribute("prodotti", prodotti);

        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ProdottoForm());
        }

        return "produttore/index";
    }
}
