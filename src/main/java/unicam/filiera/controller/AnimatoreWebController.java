package unicam.filiera.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.filiera.dto.EventoTipo;
import unicam.filiera.dto.VisitaInvitoDto;
import unicam.filiera.dto.FieraDto;
import unicam.filiera.service.FieraService;
import unicam.filiera.service.UtenteService;
import unicam.filiera.service.VisitaInvitoService;

@Controller
@RequestMapping("/animatore")
public class AnimatoreWebController {

    private static final Logger log = LoggerFactory.getLogger(AnimatoreWebController.class);

    private final VisitaInvitoService visitaService;
    private final FieraService fieraService;
    private final UtenteService utenteService;

    @Autowired
    public AnimatoreWebController(VisitaInvitoService visitaService,
                                  FieraService fieraService,
                                  UtenteService utenteService) {
        this.visitaService = visitaService;
        this.fieraService = fieraService;
        this.utenteService = utenteService;
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
        log.info("Accesso alla dashboard animatore da utente '{}'", username);

        // Preparo DTO vuoti se non già presenti
        if (!model.containsAttribute("visitaDto")) {
            VisitaInvitoDto v = new VisitaInvitoDto();
            v.setTipo(EventoTipo.VISITA);
            model.addAttribute("visitaDto", v);
            log.debug("Creato nuovo VisitaInvitoDto per utente '{}'", username);
        }
        if (!model.containsAttribute("fieraDto")) {
            FieraDto f = new FieraDto();
            f.setTipo(EventoTipo.FIERA);
            model.addAttribute("fieraDto", f);
            log.debug("Creato nuovo FieraDto per utente '{}'", username);
        }

        // Carico le liste
        prepareDashboardLists(model, username);

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
        log.info("Richiesta creazione visita da utente '{}': {}", username, dto);

        if (br.hasErrors()) {
            log.warn("Validazione fallita per la visita di '{}': errori={}", username, br.getAllErrors());
            prepareDashboardLists(model, username);
            ensureDtos(model);
            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);
            return "dashboard/animatore";
        }

        try {
            visitaService.creaVisita(dto, username);
            log.info("Visita '{}' creata con successo da '{}'", dto.getNome(), username);
            ra.addFlashAttribute("createSuccessMessage", "Visita pubblicata con successo");
            return "redirect:/animatore/dashboard";
        } catch (Exception ex) {
            log.error("Errore durante creazione visita da '{}': {}", username, ex.getMessage(), ex);
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
        log.info("Richiesta eliminazione visita '{}' da parte di '{}'", id, username);
        visitaService.eliminaById(id, username);
        log.debug("Visita '{}' eliminata da '{}'", id, username);
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
        log.info("Richiesta creazione fiera da utente '{}': {}", username, dto);

        if (br.hasErrors()) {
            log.warn("Validazione fallita per la fiera di '{}': errori={}", username, br.getAllErrors());
            prepareDashboardLists(model, username);
            ensureDtos(model);
            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);
            return "dashboard/animatore";
        }

        try {
            fieraService.creaFiera(dto, username);
            log.info("Fiera '{}' creata con successo da '{}'", dto.getNome(), username);
            ra.addFlashAttribute("createSuccessMessage", "Fiera pubblicata con successo");
            return "redirect:/animatore/dashboard";
        } catch (Exception ex) {
            log.error("Errore durante creazione fiera da '{}': {}", username, ex.getMessage(), ex);
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
        log.info("Richiesta eliminazione fiera '{}' da parte di '{}'", id, username);
        fieraService.eliminaById(id, username);
        log.debug("Fiera '{}' eliminata da '{}'", id, username);
        return "Fiera eliminata con successo";
    }

    // =======================
    // Utility interna
    // =======================
    private void prepareDashboardLists(Model model, String username) {
        log.debug("Caricamento liste per dashboard animatore '{}'", username);
        model.addAttribute("visite", visitaService.getVisiteByCreatore(username));
        model.addAttribute("fiere", fieraService.getFiereByCreatore(username));
        model.addAttribute("destinatari", utenteService.getDestinatariPossibili());
    }

    private void ensureDtos(Model model) {
        if (!model.containsAttribute("visitaDto")) {
            VisitaInvitoDto v = new VisitaInvitoDto();
            v.setTipo(EventoTipo.VISITA);
            model.addAttribute("visitaDto", v);
            log.debug("Creato VisitaInvitoDto in ensureDtos()");
        }
        if (!model.containsAttribute("fieraDto")) {
            FieraDto f = new FieraDto();
            f.setTipo(EventoTipo.FIERA);
            model.addAttribute("fieraDto", f);
            log.debug("Creato FieraDto in ensureDtos()");
        }
    }
}
