package unicam.filiera_agricola_2425.controllers;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera_agricola_2425.models.Ruolo;
import unicam.filiera_agricola_2425.models.UtenteAutenticato;
import unicam.filiera_agricola_2425.repositories.UtenteRepository;
import unicam.filiera_agricola_2425.dtos.UtenteAutenticatoForm;

import java.util.Optional;


@Controller
public class HomeController {

    @Autowired
    private UtenteRepository utenteRepository;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/registrazione")
    public String showForm(Model model) {
        model.addAttribute("utente", new UtenteAutenticatoForm()); // DTO
        model.addAttribute("ruoli", Ruolo.values());
        return "registrazione";
    }

    @PostMapping("/registrazione")
    public String salvaUtente(@ModelAttribute UtenteAutenticatoForm utenteForm) {
        UtenteAutenticato utente = utenteForm.toUtente(); // factory pattern qui!
        utenteRepository.save(utente);
        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("utente", new UtenteAutenticatoForm());
        model.addAttribute("ruoli", Ruolo.values());
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute UtenteAutenticatoForm utenteForm, Model model) {
        // ⚠️ Per ora solo logica semplificata — nessun controllo password
        Optional<UtenteAutenticato> utente = utenteRepository.findByUsername(utenteForm.getUsername());

        if (utente.isPresent() &&
                utente.get().getPassword().equals(utenteForm.getPassword()) &&
                utente.get().getRuolo().equals(utenteForm.getRuolo())) {

            model.addAttribute("username", utente.get().getUsername());
            return "redirect:/marketplace"; // oppure una dashboard
        }

        // Login fallito
        model.addAttribute("errore", "Credenziali non valide");
        model.addAttribute("utente", utenteForm);
        model.addAttribute("ruoli", Ruolo.values());
        return "login";
    }


    @GetMapping("/marketplace")
    public String marketplace() {
        return "marketplace"; // pagina dummy
    }

    @GetMapping("/mappa")
    public String mappa() {
        return "mappa"; // pagina dummy
    }

    @GetMapping("/social")
    public String social() {
        return "social"; // pagina dummy
    }
}
