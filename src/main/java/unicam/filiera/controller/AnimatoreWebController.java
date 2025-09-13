package unicam.filiera.controller;

import jakarta.validation.Valid;
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
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.VisitaInvito;
import unicam.filiera.model.Fiera;
import unicam.filiera.service.FieraService;
import unicam.filiera.service.UtenteService;
import unicam.filiera.service.VisitaInvitoService;

import java.util.List;

@Controller
@RequestMapping("/animatore")
public class AnimatoreWebController {

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

        // Preparo DTO vuoti se non già presenti
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
