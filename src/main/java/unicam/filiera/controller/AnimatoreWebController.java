package unicam.filiera.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.filiera.dto.EventoTipo;
import unicam.filiera.dto.VisitaInvitoDto;
import unicam.filiera.dto.FieraDto;
import unicam.filiera.dto.RichiestaEliminazioneProfiloDto;
import unicam.filiera.entity.RichiestaEliminazioneProfiloEntity;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;
import unicam.filiera.repository.RichiestaEliminazioneProfiloRepository;
import unicam.filiera.repository.UtenteRepository;
import unicam.filiera.service.*;

@Controller
@RequestMapping("/animatore")
public class AnimatoreWebController {

    private static final Logger log = LoggerFactory.getLogger(AnimatoreWebController.class);

    private final VisitaInvitoService visitaService;
    private final FieraService fieraService;
    private final UtenteService utenteService;
    private final EliminazioneProfiloService eliminazioneProfiloService;
    private final UtenteRepository utenteRepo;
    private final RichiestaEliminazioneProfiloRepository richiestaRepo;

    @Autowired
    public AnimatoreWebController(VisitaInvitoService visitaService,
                                  FieraService fieraService,
                                  UtenteService utenteService,
                                  EliminazioneProfiloService eliminazioneProfiloService,
                                  UtenteRepository utenteRepo,
                                  RichiestaEliminazioneProfiloRepository richiestaRepo) {
        this.visitaService = visitaService;
        this.fieraService = fieraService;
        this.utenteService = utenteService;
        this.eliminazioneProfiloService = eliminazioneProfiloService;
        this.utenteRepo = utenteRepo;
        this.richiestaRepo = richiestaRepo;
    }

    private String resolveUsername(Authentication auth) {
        return (auth != null) ? auth.getName() : "demo_user";
    }

    /* ======================= DASHBOARD ======================= */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        String username = resolveUsername(auth);
        log.info("[Dashboard Animatore] Utente autenticato={}", username);

        ensureDtos(model);
        prepareDashboardLists(model, username);

        utenteRepo.findByUsername(username).ifPresentOrElse(
                u -> model.addAttribute("utente", u),
                () -> log.warn("[Dashboard Animatore] Utente {} non trovato in DB!", username)
        );

        model.addAttribute("showForm", false);
        return "dashboard/animatore";
    }

    /* ======================= VISITE ======================= */
    @PostMapping("/crea-visita")
    public String creaVisita(@Valid @ModelAttribute("visitaDto") VisitaInvitoDto dto,
                             BindingResult br,
                             Authentication auth,
                             RedirectAttributes ra,
                             Model model) {
        String username = resolveUsername(auth);

        if (br.hasErrors()) {
            prepareDashboardLists(model, username);
            ensureDtos(model);
            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);
            return "dashboard/animatore";
        }

        try {
            visitaService.creaVisita(dto, username);
            ra.addFlashAttribute("createSuccessMessage", "Visita pubblicata con successo");
            return "redirect:/animatore/dashboard";
        } catch (Exception ex) {
            log.error("Errore durante creazione visita", ex);
            prepareDashboardLists(model, username);
            ensureDtos(model);
            model.addAttribute("errorMessage", "Errore: " + ex.getMessage());
            return "dashboard/animatore";
        }
    }

    @DeleteMapping("/elimina-visita/{id}")
    @ResponseBody
    public String eliminaVisita(@PathVariable Long id, Authentication auth) {
        String username = resolveUsername(auth);
        visitaService.eliminaById(id, username);
        return "Visita eliminata con successo";
    }

    /* ======================= FIERE ======================= */
    @PostMapping("/crea-fiera")
    public String creaFiera(@Valid @ModelAttribute("fieraDto") FieraDto dto,
                            BindingResult br,
                            Authentication auth,
                            RedirectAttributes ra,
                            Model model) {
        String username = resolveUsername(auth);

        if (br.hasErrors()) {
            prepareDashboardLists(model, username);
            ensureDtos(model);
            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);
            return "dashboard/animatore";
        }

        try {
            fieraService.creaFiera(dto, username);
            ra.addFlashAttribute("createSuccessMessage", "Fiera pubblicata con successo");
            return "redirect:/animatore/dashboard";
        } catch (Exception ex) {
            log.error("Errore durante creazione fiera", ex);
            prepareDashboardLists(model, username);
            ensureDtos(model);
            model.addAttribute("errorMessage", "Errore: " + ex.getMessage());
            return "dashboard/animatore";
        }
    }

    @DeleteMapping("/elimina-fiera/{id}")
    @ResponseBody
    public String eliminaFiera(@PathVariable Long id, Authentication auth) {
        String username = resolveUsername(auth);
        fieraService.eliminaById(id, username);
        return "Fiera eliminata con successo";
    }

    /* ======================= ELIMINAZIONE PROFILO ======================= */
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

            return ResponseEntity.ok("Richiesta di eliminazione inviata con successo.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
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

    /* ======================= Utility interna ======================= */
    private void prepareDashboardLists(Model model, String username) {
        model.addAttribute("visite", visitaService.getVisiteByCreatore(username));
        model.addAttribute("fiere", fieraService.getFiereByCreatore(username));
        model.addAttribute("destinatari", utenteService.getDestinatariPossibili());
    }

    private void ensureDtos(Model model) {
        if (!model.containsAttribute("visitaDto")) {
            VisitaInvitoDto v = new VisitaInvitoDto();
            v.setTipo(EventoTipo.VISITA);
            model.addAttribute("visitaDto", v);
        }
        if (!model.containsAttribute("fieraDto")) {
            FieraDto f = new FieraDto();
            f.setTipo(EventoTipo.FIERA);
            model.addAttribute("fieraDto", f);
        }
    }
}
