// src/main/java/unicam/progetto_filiera_springboot/controller/LoginController.java
package unicam.progetto_filiera_springboot.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import unicam.progetto_filiera_springboot.dto.auth.LoginDto;
import unicam.progetto_filiera_springboot.domain.model.Ruolo;
import unicam.progetto_filiera_springboot.domain.actor.UtenteAutenticato; // <-- attore dominio
import unicam.progetto_filiera_springboot.application.service.AuthService;
import unicam.progetto_filiera_springboot.strategy.validation.ValidationException;

@Controller
@RequestMapping("/login")
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("dto", new LoginDto());
        model.addAttribute("ruoli", Ruolo.values());
        return "login/login";
    }

    @PostMapping
    public String submit(@ModelAttribute("dto") LoginDto dto, Model model, HttpSession session) {
        try {
            // Usa il servizio che ritorna l'ATTore di dominio (non la entity JPA)
            UtenteAutenticato attore = authService.loginAsActor(dto);

            // Sessione "leggera": salvo l'attore
            session.setAttribute("attore", attore);

            // Redirect per ruolo
            switch (attore.getRuolo()) {
                case PRODUTTORE -> {
                    return "redirect:/produttore";
                }
                case TRASFORMATORE -> {
                    return "redirect:/trasformatore";
                }
                case DISTRIBUTORE_TIPICITA -> {
                    return "redirect:/distributore";
                }
                case CURATORE -> {
                    return "redirect:/curatore";
                }
                case ANIMATORE -> {
                    return "redirect:/animatore";
                }
                case GESTORE_PIATTAFORMA -> {
                    return "redirect:/gestore";
                }
                case ACQUIRENTE -> {
                    return "redirect:/acquirente";
                }
            }

            // (fallback) dovrebbe essere impossibile arrivarci
            model.addAttribute("success", "Benvenuto " + attore.getNome() + " (" + attore.getRuolo() + ")");
            model.addAttribute("ruoli", Ruolo.values());
            return "login/login";

        } catch (ValidationException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("ruoli", Ruolo.values());
            return "login/login";
        }
    }
}
