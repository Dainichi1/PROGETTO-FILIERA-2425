package unicam.filiera.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.UtenteDto;
import unicam.filiera.model.CategoriaContenuto;
import unicam.filiera.model.CriteriRicerca;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;
import unicam.filiera.service.EliminazioneProfiloService;
import unicam.filiera.service.GestoreContenutiService;
import unicam.filiera.service.UtenteService;

@Controller
@RequestMapping("/gestore")
public class GestoreWebController {

    private static final Logger log = LoggerFactory.getLogger(GestoreWebController.class);

    private final UtenteService utenteService;
    private final GestoreContenutiService contenutiService;
    private final EliminazioneProfiloService eliminazioneProfiloService;

    public GestoreWebController(UtenteService utenteService,
                                GestoreContenutiService contenutiService,
                                EliminazioneProfiloService eliminazioneProfiloService) {
        this.utenteService = utenteService;
        this.contenutiService = contenutiService;
        this.eliminazioneProfiloService = eliminazioneProfiloService;
    }

    /* ================== DASHBOARD ================== */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = auth.getName();
        UtenteDto gestore = utenteService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        model.addAttribute("utente", gestore);
        return "dashboard/gestore";
    }

    /* ================== CONTENUTI ================== */
    @GetMapping("/contenuti")
    public String contenuti(Model model) {
        model.addAttribute("categorie", contenutiService.getCategorieContenuti());
        model.addAttribute("contenuti", null);
        return "dashboard/contenuti";
    }

    @GetMapping("/contenuti/{categoria}")
    public String contenutiCategoria(
            @PathVariable("categoria") CategoriaContenuto categoria,
            @RequestParam(required = false) String testo,
            @RequestParam(required = false) String stato,
            @RequestParam(required = false, defaultValue = "nome") String ordinamento,
            @RequestParam(required = false, defaultValue = "true") boolean crescente,
            Model model) {

        var lista = contenutiService.getContenutiCategoria(categoria);

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

    /* ================== RICHIESTA ELIMINAZIONE PROFILO ================== */
    @PostMapping("/richiesta-eliminazione")
    @ResponseBody
    public ResponseEntity<String> richiestaEliminazione(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Utente non autenticato");
        }

        String username = auth.getName();

        try {
            eliminazioneProfiloService.inviaRichiestaEliminazione(
                    unicam.filiera.dto.RichiestaEliminazioneProfiloDto.builder()
                            .username(username)
                            .build()
            );
            log.info("Richiesta eliminazione profilo inviata per utente={}", username);
            return ResponseEntity.ok("Richiesta di eliminazione inviata con successo.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            log.error("Errore nella richiesta eliminazione per utente {}", username, e);
            return ResponseEntity.internalServerError().body("Errore: " + e.getMessage());
        }
    }

    /* ================== GESTIONE RICHIESTE ================== */
    @PostMapping("/richieste/{id}/rifiuta")
    @ResponseBody
    public ResponseEntity<String> rifiutaRichiesta(@PathVariable Long id) {
        eliminazioneProfiloService.aggiornaStato(id, StatoRichiestaEliminazioneProfilo.RIFIUTATA);
        return ResponseEntity.ok("Richiesta rifiutata");
    }

    @PostMapping("/richieste/{id}/accetta")
    @ResponseBody
    public ResponseEntity<String> accettaRichiesta(@PathVariable Long id) {
        eliminazioneProfiloService.aggiornaStato(id, StatoRichiestaEliminazioneProfilo.APPROVATA);
        return ResponseEntity.ok("Richiesta approvata");
    }

    @GetMapping("/richiesta-eliminazione/stato")
    @ResponseBody
    public ResponseEntity<String> statoRichiesta(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("NON_AUTENTICATO");
        }

        String username = auth.getName();

        return eliminazioneProfiloService.getRichiesteByUtente(username).stream()
                .filter(r -> StatoRichiestaEliminazioneProfilo.valueOf(r.getStato())
                        == StatoRichiestaEliminazioneProfilo.APPROVATA)
                .max((a, b) -> a.getDataRichiesta().compareTo(b.getDataRichiesta())) // prendo la più recente
                .map(r -> ResponseEntity.ok("APPROVATA:" + r.getId()))
                .orElse(ResponseEntity.ok("NESSUNA"));
    }
}
