package unicam.filiera.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.Ruolo;
import unicam.filiera.repository.UtenteRepository;
import unicam.filiera.factory.UtenteFactory;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final UtenteRepository repo;

    public DashboardController(UtenteRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String dashboard(Authentication auth, Model model) {
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Accesso alla dashboard senza autenticazione.");
            return "redirect:/login";
        }

        String username = auth.getName();

        return repo.findById(username)
                .map(e -> {
                    UtenteAutenticato domain = (UtenteAutenticato) UtenteFactory.creaAttore(
                            e.getUsername(),
                            e.getPassword(),
                            e.getNome(),
                            e.getCognome(),
                            e.getRuolo(),
                            (e.getRuolo() == Ruolo.ACQUIRENTE && e.getFondi() != null) ? e.getFondi() : 0.0
                    );

                    model.addAttribute("utente", domain);

                    // Redireziona ai controller specifici in base al ruolo
                    switch (e.getRuolo()) {
                        case PRODUTTORE -> {
                            log.info("Utente '{}' con ruolo PRODUTTORE → redirect /produttore/dashboard", username);
                            return "redirect:/produttore/dashboard";
                        }
                        case ACQUIRENTE -> {
                            log.info("Utente '{}' con ruolo ACQUIRENTE → redirect /acquirente/dashboard", username);
                            return "redirect:/acquirente/dashboard";
                        }
                        case CURATORE -> {
                            log.info("Utente '{}' con ruolo CURATORE → redirect /curatore/dashboard", username);
                            return "redirect:/curatore/dashboard";
                        }
                        case DISTRIBUTORE_TIPICITA -> {
                            log.info("Utente '{}' con ruolo DISTRIBUTORE_TIPICITA → redirect /distributore/dashboard", username);
                            return "redirect:/distributore/dashboard";
                        }
                        case TRASFORMATORE -> {
                            log.info("Utente '{}' con ruolo TRASFORMATORE → redirect /trasformatore/dashboard", username);
                            return "redirect:/trasformatore/dashboard";
                        }
                        case ANIMATORE -> {
                            log.info("Utente '{}' con ruolo ANIMATORE → redirect /animatore/dashboard", username);
                            return "redirect:/animatore/dashboard";
                        }
                        case GESTORE_PIATTAFORMA -> {
                            log.info("Utente '{}' con ruolo GESTORE → redirect /gestore/dashboard", username);
                            return "redirect:/gestore/dashboard";
                        }
                        default -> {
                            log.error("Ruolo '{}' non gestito per utente '{}'", e.getRuolo(), username);
                            return "error/ruolo_non_gestito";
                        }
                    }
                })
                .orElseGet(() -> {
                    log.error("Utente '{}' non trovato in DB.", username);
                    return "error/utente_non_trovato";
                });
    }
}
