package unicam.filiera_agricola_2425.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera_agricola_2425.models.UtenteAutenticato;
import unicam.filiera_agricola_2425.repositories.UtenteRepository;

import java.util.Optional;

@Controller
@RequestMapping("/curatore")
public class CuratoreController {

    @Autowired
    private UtenteRepository utenteRepository;

    @GetMapping("/dashboard")
    public String dashboardCuratore(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Optional<UtenteAutenticato> utenteOpt = utenteRepository.findByUsername(username);
        if (utenteOpt.isEmpty()) return "redirect:/login";

        UtenteAutenticato utente = utenteOpt.get();
        model.addAttribute("nome", utente.getNome());
        model.addAttribute("ruolo", utente.getRuolo());

        return "curatore_dashboard";
    }
}
