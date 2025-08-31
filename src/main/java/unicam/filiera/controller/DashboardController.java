package unicam.filiera.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.model.UtenteAutenticato;
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

    @GetMapping("/{username}")
    public String dashboard(@PathVariable String username, Model model) {
        return repo.findById(username)
                .map(e -> {
                    UtenteAutenticato domain = (UtenteAutenticato) UtenteFactory.creaAttore(
                            e.getUsername(),
                            e.getPassword(),
                            e.getNome(),
                            e.getCognome(),
                            e.getRuolo(),
                            e.getFondi() != null ? e.getFondi() : 0.0
                    );
                    String view = viewFactory.viewFor(e.getRuolo(), model, domain);
                    log.info("Utente {} con ruolo {} → view {}", username, e.getRuolo(), view);
                    return view;
                })
                .orElse("error/utente_non_trovato");
    }
}
