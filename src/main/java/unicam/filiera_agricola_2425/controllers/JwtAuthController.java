package unicam.filiera_agricola_2425.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera_agricola_2425.dtos.UtenteAutenticatoForm;
import unicam.filiera_agricola_2425.models.UtenteAutenticato;
import unicam.filiera_agricola_2425.repositories.UtenteRepository;
import unicam.filiera_agricola_2425.security.JwtUtil;

import java.util.Optional;

@Controller
public class JwtAuthController {

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/jwt-login")
    public String showLoginForm(Model model) {
        model.addAttribute("utente", new UtenteAutenticatoForm());
        return "jwt_login";
    }

    @PostMapping("/jwt-login")
    public String login(@ModelAttribute UtenteAutenticatoForm utenteForm, Model model) {
        Optional<UtenteAutenticato> utenteOpt = utenteRepository.findByUsername(utenteForm.getUsername());

        if (utenteOpt.isPresent()) {
            UtenteAutenticato utente = utenteOpt.get();
            if (utente.getPassword().equals(utenteForm.getPassword()) &&
                    utente.getRuolo().equals(utenteForm.getRuolo())) {

                String token = jwtUtil.generateToken(utente.getUsername(), utente.getRuolo().toString());

                model.addAttribute("nome", utente.getNome());
                model.addAttribute("ruolo", utente.getRuolo());
                model.addAttribute("token", token);
                return "dashboard";
            }
        }

        model.addAttribute("errore", "Errore campi errati");
        return "jwt_login";
    }
}
