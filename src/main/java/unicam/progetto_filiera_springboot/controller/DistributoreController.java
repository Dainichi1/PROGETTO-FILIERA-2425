package unicam.progetto_filiera_springboot.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import unicam.progetto_filiera_springboot.domain.actor.DistributoreTipicita;


@Controller
public class DistributoreController {

    @GetMapping("/distributore")
    public String home(HttpSession session, Model model) {
        Object attore = session.getAttribute("attore");

        // Consente l’accesso solo se in sessione c’è un Produttore
        if (!(attore instanceof DistributoreTipicita p)) {
            return "redirect:/login";
        }

        model.addAttribute("username", p.getUsername());
        model.addAttribute("ruolo", p.getRuolo());
        return "distributore/index";
    }
}