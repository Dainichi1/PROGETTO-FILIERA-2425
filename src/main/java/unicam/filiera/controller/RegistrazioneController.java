package unicam.filiera.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.service.UtenteService;

@Controller
public class RegistrazioneController {

    private final UtenteService service;

    public RegistrazioneController(UtenteService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/register")
    public String mostraForm(Model model) {
        model.addAttribute("utente", new UtenteEntity());
        return "register";
    }

    @PostMapping("/register")
    public String registraUtente(
            @Valid @ModelAttribute("utente") UtenteEntity utente,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "register"; // mostra errori campo per campo
        }

        RegistrazioneEsito esito = service.registraUtente(utente);

        switch (esito) {
            case SUCCESSO:
                model.addAttribute("success", "Registrazione completata!");
                return "index"; // ritorna alla home con messaggio
            case USERNAME_GIA_ESISTENTE:
                model.addAttribute("error", "⚠ Username già registrato.");
                break;
            case PERSONA_GIA_REGISTRATA:
                model.addAttribute("error", "⚠ Persona già registrata.");
                break;
        }

        return "register";
    }
}
