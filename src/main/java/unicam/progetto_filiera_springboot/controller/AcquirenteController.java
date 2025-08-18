package unicam.progetto_filiera_springboot.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import unicam.progetto_filiera_springboot.domain.actor.Acquirente;

@Controller
public class AcquirenteController {

    @GetMapping("/acquirente")
    public String home(HttpSession session, Model model) {
        Object attore = session.getAttribute("attore");

        if (!(attore instanceof Acquirente a)) {
            return "redirect:/login";
        }

        model.addAttribute("username", a.getUsername());
        model.addAttribute("ruolo", a.getRuolo());
        model.addAttribute("fondi", a.getFondi());

        return "acquirente/index";
    }
}
