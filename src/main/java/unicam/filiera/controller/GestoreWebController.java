package unicam.filiera.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.RichiestaEliminazioneProfiloDto;
import unicam.filiera.entity.RichiestaEliminazioneProfiloEntity;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.*;
import unicam.filiera.observer.EliminazioneProfiloNotifier;
import unicam.filiera.repository.RichiestaEliminazioneProfiloRepository;
import unicam.filiera.repository.UtenteRepository;
import unicam.filiera.service.EliminazioneProfiloService;
import unicam.filiera.service.GestoreContenutiService;

@Controller
@RequestMapping("/gestore")
public class GestoreWebController {

    private static final Logger log = LoggerFactory.getLogger(GestoreWebController.class);

    private final UtenteRepository repo;
    private final GestoreContenutiService contenutiService;
    private final EliminazioneProfiloService eliminazioneProfiloService;
    private final RichiestaEliminazioneProfiloRepository richiestaRepo;
    private final EliminazioneProfiloNotifier notifier;
    private final NotificationController notificationController; // <-- per SSE

    public GestoreWebController(UtenteRepository repo,
                                GestoreContenutiService contenutiService,
                                EliminazioneProfiloService eliminazioneProfiloService,
                                RichiestaEliminazioneProfiloRepository richiestaRepo,
                                EliminazioneProfiloNotifier notifier,
                                NotificationController notificationController) {
        this.repo = repo;
        this.contenutiService = contenutiService;
        this.eliminazioneProfiloService = eliminazioneProfiloService;
        this.richiestaRepo = richiestaRepo;
        this.notifier = notifier;
        this.notificationController = notificationController;
    }

    /* ================== DASHBOARD ================== */
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
                    return "dashboard/gestore";
                })
                .orElse("error/utente_non_trovato");
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

        String loginName = auth.getName();
        UtenteEntity utente = repo.findByUsername(loginName)
                .orElseThrow(() -> new IllegalStateException("Utente non trovato"));

        try {
            RichiestaEliminazioneProfiloDto dto = RichiestaEliminazioneProfiloDto.builder()
                    .username(utente.getUsername())
                    .build();

            eliminazioneProfiloService.inviaRichiestaEliminazione(dto);

            log.info("Richiesta eliminazione profilo inviata per utente={}", utente.getUsername());
            return ResponseEntity.ok("Richiesta di eliminazione inviata con successo.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            log.error("Errore nella richiesta eliminazione per utente {}", utente.getUsername(), e);
            return ResponseEntity.internalServerError().body("Errore: " + e.getMessage());
        }
    }

    /* ================== GESTIONE RICHIESTE ================== */
    @PostMapping("/richieste/{id}/rifiuta")
    @ResponseBody
    public ResponseEntity<String> rifiutaRichiesta(@PathVariable Long id) {
        return richiestaRepo.findById(id)
                .map(entity -> {
                    entity.setStato(StatoRichiestaEliminazioneProfilo.RIFIUTATA);
                    richiestaRepo.save(entity);

                    var model = toModel(entity);
                    notifier.notificaRifiutata(model);
                    notificationController.notifyUser(model.getUsername(),
                            "RIFIUTATA",
                            "La tua richiesta di eliminazione è stata rifiutata.");

                    log.info("Richiesta {} rifiutata", id);

                    return ResponseEntity.ok("Richiesta rifiutata");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/richieste/{id}/accetta")
    @ResponseBody
    public ResponseEntity<String> accettaRichiesta(@PathVariable Long id) {
        return richiestaRepo.findById(id)
                .map(entity -> {
                    // 1. Aggiorna stato richiesta
                    entity.setStato(StatoRichiestaEliminazioneProfilo.APPROVATA);
                    richiestaRepo.save(entity);

                    log.info("Richiesta {} approvata per utente {}", id, entity.getUsername());
                    return ResponseEntity.ok("Richiesta approvata");
                })
                .orElse(ResponseEntity.notFound().build());
    }



    /* ================== POLLING STATO RICHIESTA ================== */
    @GetMapping("/richiesta-eliminazione/stato")
    @ResponseBody
    public ResponseEntity<String> statoRichiesta(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("NON_AUTENTICATO");
        }
        String username = auth.getName();

        return richiestaRepo.findFirstByUsernameAndStatoOrderByDataRichiestaDesc(
                        username, StatoRichiestaEliminazioneProfilo.APPROVATA)
                .map(RichiestaEliminazioneProfiloEntity::getId)
                .map(id -> ResponseEntity.ok("APPROVATA:" + id))
                .orElse(ResponseEntity.ok("NESSUNA"));
    }

    /* ================== MAPPER ================== */
    private RichiestaEliminazioneProfilo toModel(RichiestaEliminazioneProfiloEntity entity) {
        return new RichiestaEliminazioneProfilo.Builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .stato(entity.getStato())
                .dataRichiesta(entity.getDataRichiesta())
                .build();
    }
}
