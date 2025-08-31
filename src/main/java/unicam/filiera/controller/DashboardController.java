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
import unicam.filiera.factory.ViewFactory;
import unicam.filiera.factory.UtenteFactory;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    private final UtenteRepository repo;
    private final ViewFactory viewFactory;

    public DashboardController(UtenteRepository repo, ViewFactory viewFactory) {
        this.repo = repo;
        this.viewFactory = viewFactory;
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

                    String view = viewFactory.viewFor(e.getRuolo(), model, domain);
                    log.info("Utente '{}' con ruolo '{}' → view '{}'", username, e.getRuolo(), view);
                    return view;
                })
                .orElseGet(() -> {
                    log.error("Utente '{}' non trovato in DB.", username);
                    return "error/utente_non_trovato";
                });
    }
}
