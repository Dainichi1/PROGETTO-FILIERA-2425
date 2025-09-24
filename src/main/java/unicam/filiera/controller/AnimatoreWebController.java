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
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.service.*;

@Controller
@RequestMapping("/animatore")
public class AnimatoreWebController {

    private static final Logger log = LoggerFactory.getLogger(AnimatoreWebController.class);

    private final VisitaInvitoService visitaService;
    private final FieraService fieraService;
    private final UtenteService utenteService;
    private final EliminazioneProfiloService eliminazioneProfiloService;
    private final unicam.filiera.repository.UtenteRepository utenteRepo;

    @Autowired
    public AnimatoreWebController(VisitaInvitoService visitaService,
                                  FieraService fieraService,
                                  UtenteService utenteService,
                                  EliminazioneProfiloService eliminazioneProfiloService,
                                  unicam.filiera.repository.UtenteRepository utenteRepo) {
        this.visitaService = visitaService;
        this.fieraService = fieraService;
        this.utenteService = utenteService;
        this.eliminazioneProfiloService = eliminazioneProfiloService;
        this.utenteRepo = utenteRepo;
    }

    private String resolveUsername(Authentication auth) {
        return (auth != null) ? auth.getName() : "demo_user";
    }

    // =======================
    // Dashboard
    // =======================

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        String username = resolveUsername(auth);
        log.info("[Dashboard Animatore] Utente autenticato={}", username);

        // Preparo DTO se non presenti
        ensureDtos(model);

        // Carico le liste
        prepareDashboardLists(model, username);

        // Aggiungo l'utente loggato al model
        utenteRepo.findByUsername(username).ifPresentOrElse(
                u -> {
                    log.info("[Dashboard Animatore] Caricato utente da DB: username={}, ruolo={}", u.getUsername(), u.getRuolo());
                    model.addAttribute("utente", u);
                },
                () -> log.warn("[Dashboard Animatore] Utente {} non trovato in DB!", username)
        );

        model.addAttribute("showForm", false);
        return "dashboard/animatore";
    }

    // =======================
    // VISITE
    // =======================

    @PostMapping("/crea-visita")
    public String creaVisita(@Valid @ModelAttribute("visitaDto") VisitaInvitoDto dto,
                             BindingResult br,
                             Authentication auth,
                             RedirectAttributes ra,
                             Model model) {
        String username = resolveUsername(auth);
        log.info("Richiesta creazione visita da '{}': {}", username, dto);

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
            log.error("Errore durante creazione visita: {}", ex.getMessage(), ex);
            prepareDashboardLists(model, username);
            ensureDtos(model);
            model.addAttribute("errorMessage", "Errore durante creazione visita: " + ex.getMessage());
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

    // =======================
    // FIERE
    // =======================

    @PostMapping("/crea-fiera")
    public String creaFiera(@Valid @ModelAttribute("fieraDto") FieraDto dto,
                            BindingResult br,
                            Authentication auth,
                            RedirectAttributes ra,
                            Model model) {
        String username = resolveUsername(auth);
        log.info("Richiesta creazione fiera da '{}': {}", username, dto);

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
            log.error("Errore durante creazione fiera: {}", ex.getMessage(), ex);
            prepareDashboardLists(model, username);
            ensureDtos(model);
            model.addAttribute("errorMessage", "Errore durante creazione fiera: " + ex.getMessage());
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

    // =======================
    // ELIMINAZIONE PROFILO
    // =======================

    @PostMapping("/richiesta-eliminazione")
    @ResponseBody
    public ResponseEntity<String> richiestaEliminazione(Authentication auth) {
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

    // =======================
    // Utility interna
    // =======================
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
