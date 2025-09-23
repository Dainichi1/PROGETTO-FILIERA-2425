package unicam.filiera.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.model.CategoriaContenuto;
import unicam.filiera.model.CriteriRicerca;
import unicam.filiera.model.GestorePiattaforma;
import unicam.filiera.repository.UtenteRepository;
import unicam.filiera.service.GestoreContenutiService;

@Controller
@RequestMapping("/gestore")
public class GestoreWebController {

    private final UtenteRepository repo;
    private final GestoreContenutiService contenutiService;

    public GestoreWebController(UtenteRepository repo,
                                GestoreContenutiService contenutiService) {
        this.repo = repo;
        this.contenutiService = contenutiService;
    }

    /**
     * Dashboard iniziale: solo messaggio di benvenuto + pulsante.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = auth.getName();

        return repo.findById(username)
                .map(e -> {
                    GestorePiattaforma gestore = new GestorePiattaforma(
                            e.getUsername(),
                            e.getPassword(),
                            e.getNome(),
                            e.getCognome()
                    );
                    model.addAttribute("utente", gestore);
                    return "dashboard/gestore"; // view: benvenuto + pulsante
                })
                .orElse("error/utente_non_trovato");
    }

    /**
     * Schermata riepilogo contenuti (categorie a sinistra, lista a destra).
     */
    @GetMapping("/contenuti")
    public String contenuti(Model model) {
        model.addAttribute("categorie", contenutiService.getCategorieContenuti());
        model.addAttribute("contenuti", null); // inizialmente nessun contenuto caricato
        return "dashboard/contenuti";
    }

    /**
     * Quando il gestore seleziona una categoria, con eventuali filtri/ordinamento.
     */
    @GetMapping("/contenuti/{categoria}")
    public String contenutiCategoria(
            @PathVariable("categoria") CategoriaContenuto categoria,
            @RequestParam(required = false) String testo,
            @RequestParam(required = false) String stato,
            @RequestParam(required = false, defaultValue = "nome") String ordinamento,
            @RequestParam(required = false, defaultValue = "true") boolean crescente,
            Model model) {

        var lista = contenutiService.getContenutiCategoria(categoria);

        // costruiamo criteri da query string
        var criteri = CriteriRicerca.builder()
                .testo(testo)
                .stato(stato)
                .ordinamento(ordinamento)
                .crescente(crescente)
                .build();

        var filtrata = contenutiService.filtraOrdinaLista(lista, criteri);

        model.addAttribute("categorie", contenutiService.getCategorieContenuti());
        model.addAttribute("contenuti", filtrata);
        model.addAttribute("categoriaSelezionata", categoria);
        model.addAttribute("statiPossibili", contenutiService.getPossibiliStati(categoria));
        model.addAttribute("criteri", criteri);

        return "dashboard/contenuti";
    }
}
