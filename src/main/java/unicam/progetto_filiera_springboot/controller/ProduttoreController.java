package unicam.progetto_filiera_springboot.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import unicam.progetto_filiera_springboot.application.dto.ProdottoForm;
import unicam.progetto_filiera_springboot.domain.actor.Produttore;

@Controller
@RequestMapping("/produttore")
public class ProduttoreController {

    @GetMapping
    public String index(HttpSession session, Model model) {
        Object attore = session.getAttribute("attore");

        // Consente l’accesso solo se in sessione c’è un Produttore
        if (!(attore instanceof Produttore p)) {
            return "redirect:/login";
        }

        // dati per header
        model.addAttribute("username", p.getUsername());
        model.addAttribute("ruolo", p.getRuolo());


        if (!model.containsAttribute("form")) {
            ProdottoForm form = new ProdottoForm();

            model.addAttribute("form", form);
        }

        return "produttore/index";
    }
}
