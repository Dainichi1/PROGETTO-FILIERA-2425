package unicam.filiera.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.dto.RichiestaEliminazioneProfiloDto;
import unicam.filiera.entity.RichiestaEliminazioneProfiloEntity;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.ProdottoTrasformato;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;
import unicam.filiera.repository.RichiestaEliminazioneProfiloRepository;
import unicam.filiera.repository.UtenteRepository;
import unicam.filiera.service.*;

import java.util.List;

@Controller
@RequestMapping("/curatore")
public class CuratoreWebController {

    private static final Logger log = LoggerFactory.getLogger(CuratoreWebController.class);

    private final ProdottoService prodottoService;
    private final PacchettoService pacchettoService;
    private final ProdottoTrasformatoService prodottoTrasformatoService;
    private final EliminazioneProfiloService eliminazioneProfiloService;
    private final UtenteRepository utenteRepo;
    private final RichiestaEliminazioneProfiloRepository richiestaRepo;

    public CuratoreWebController(ProdottoService prodottoService,
                                 PacchettoService pacchettoService,
                                 ProdottoTrasformatoService prodottoTrasformatoService,
                                 EliminazioneProfiloService eliminazioneProfiloService,
                                 UtenteRepository utenteRepo,
                                 RichiestaEliminazioneProfiloRepository richiestaRepo) {
        this.prodottoService = prodottoService;
        this.pacchettoService = pacchettoService;
        this.prodottoTrasformatoService = prodottoTrasformatoService;
        this.eliminazioneProfiloService = eliminazioneProfiloService;
        this.utenteRepo = utenteRepo;
        this.richiestaRepo = richiestaRepo;
    }

    /**
     * Dashboard del curatore con prodotti, pacchetti e trasformati in attesa.
     */
    @GetMapping("/dashboard")
    public String dashboardCuratore(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = auth.getName();
        log.info("[Dashboard Curatore] Utente autenticato={}", username);

        utenteRepo.findByUsername(username).ifPresentOrElse(
                u -> model.addAttribute("utente", u),
                () -> log.warn("[Dashboard Curatore] Utente {} non trovato in DB!", username)
        );

        // ora usiamo DTO invece del domain model
        List<ProdottoDto> prodottiInAttesa = prodottoService.getProdottiByStato(StatoProdotto.IN_ATTESA);
        List<PacchettoDto> pacchettiInAttesa = pacchettoService.getPacchettiByStato(StatoProdotto.IN_ATTESA);
        List<ProdottoTrasformatoDto> trasformatiInAttesa = prodottoTrasformatoService.getProdottiTrasformatiByStato(StatoProdotto.IN_ATTESA);

        model.addAttribute("prodotti", prodottiInAttesa);
        model.addAttribute("pacchetti", pacchettiInAttesa);
        model.addAttribute("trasformati", trasformatiInAttesa);

        return "dashboard/curatore";
    }

    /* ===============================
       PRODOTTI
       =============================== */

    @PostMapping("/approvaProdotto")
    public String approvaProdotto(@RequestParam("nome") String nome,
                                  @RequestParam("creatore") String creatore) {
        try {
            prodottoService.cambiaStatoProdotto(nome, creatore, StatoProdotto.APPROVATO, null);
            log.info("[Curatore] Prodotto '{}' approvato (creatore={})", nome, creatore);
        } catch (Exception e) {
            log.error("[Curatore] Errore approvazione prodotto '{}': {}", nome, e.getMessage(), e);
        }
        return "redirect:/curatore/dashboard";
    }

    @PostMapping("/rifiutaProdotto")
    public String rifiutaProdotto(@RequestParam("nome") String nome,
                                  @RequestParam("creatore") String creatore,
                                  @RequestParam(value = "commento", required = false) String commento) {
        try {
            prodottoService.cambiaStatoProdotto(nome, creatore, StatoProdotto.RIFIUTATO, commento);
            log.info("[Curatore] Prodotto '{}' rifiutato (creatore={}, commento={})", nome, creatore, commento);
        } catch (Exception e) {
            log.error("[Curatore] Errore rifiuto prodotto '{}': {}", nome, e.getMessage(), e);
        }
        return "redirect:/curatore/dashboard";
    }

    /* ===============================
       PACCHETTI
       =============================== */

    @PostMapping("/approvaPacchetto")
    public String approvaPacchetto(@RequestParam("nome") String nome,
                                   @RequestParam("creatore") String creatore) {
        try {
            pacchettoService.cambiaStatoPacchetto(nome, creatore, StatoProdotto.APPROVATO, null);
            log.info("[Curatore] Pacchetto '{}' approvato (creatore={})", nome, creatore);
        } catch (Exception e) {
            log.error("[Curatore] Errore approvazione pacchetto '{}': {}", nome, e.getMessage(), e);
        }
        return "redirect:/curatore/dashboard";
    }

    @PostMapping("/rifiutaPacchetto")
    public String rifiutaPacchetto(@RequestParam("nome") String nome,
                                   @RequestParam("creatore") String creatore,
                                   @RequestParam(value = "commento", required = false) String commento) {
        try {
            pacchettoService.cambiaStatoPacchetto(nome, creatore, StatoProdotto.RIFIUTATO, commento);
            log.info("[Curatore] Pacchetto '{}' rifiutato (creatore={}, commento={})", nome, creatore, commento);
        } catch (Exception e) {
            log.error("[Curatore] Errore rifiuto pacchetto '{}': {}", nome, e.getMessage(), e);
        }
        return "redirect:/curatore/dashboard";
    }

    /* ===============================
       TRASFORMATI
       =============================== */

    @PostMapping("/approvaTrasformato")
    public String approvaTrasformato(@RequestParam("nome") String nome,
                                     @RequestParam("creatore") String creatore) {
        try {
            prodottoTrasformatoService.cambiaStatoProdottoTrasformato(nome, creatore, StatoProdotto.APPROVATO, null);
            log.info("[Curatore] Trasformato '{}' approvato (creatore={})", nome, creatore);
        } catch (Exception e) {
            log.error("[Curatore] Errore approvazione trasformato '{}': {}", nome, e.getMessage(), e);
        }
        return "redirect:/curatore/dashboard";
    }

    @PostMapping("/rifiutaTrasformato")
    public String rifiutaTrasformato(@RequestParam("nome") String nome,
                                     @RequestParam("creatore") String creatore,
                                     @RequestParam(value = "commento", required = false) String commento) {
        try {
            prodottoTrasformatoService.cambiaStatoProdottoTrasformato(nome, creatore, StatoProdotto.RIFIUTATO, commento);
            log.info("[Curatore] Trasformato '{}' rifiutato (creatore={}, commento={})", nome, creatore, commento);
        } catch (Exception e) {
            log.error("[Curatore] Errore rifiuto trasformato '{}': {}", nome, e.getMessage(), e);
        }
        return "redirect:/curatore/dashboard";
    }

    /* ================== ELIMINAZIONE PROFILO ================== */
    @PostMapping("/richiesta-eliminazione")
    @ResponseBody
    public ResponseEntity<String> richiestaEliminazione(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Utente non autenticato");
        }

        String loginName = auth.getName();
        UtenteEntity utente = utenteRepo.findByUsername(loginName)
                .orElseThrow(() -> new IllegalStateException("Utente non trovato"));

        try {
            RichiestaEliminazioneProfiloDto dto = RichiestaEliminazioneProfiloDto.builder()
                    .username(utente.getUsername())
                    .build();
            eliminazioneProfiloService.inviaRichiestaEliminazione(dto);

            log.info("[Curatore] ✅ Richiesta eliminazione profilo inviata per '{}'", loginName);
            return ResponseEntity.ok("Richiesta di eliminazione inviata con successo.");
        } catch (IllegalStateException e) {
            log.warn("[Curatore] ⚠️ Richiesta duplicata per '{}': {}", loginName, e.getMessage());
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            log.error("[Curatore] ❌ Errore eliminazione profilo '{}': {}", loginName, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Errore: " + e.getMessage());
        }
    }

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
}
