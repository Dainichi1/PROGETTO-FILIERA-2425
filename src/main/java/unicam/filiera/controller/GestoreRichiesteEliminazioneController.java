package unicam.filiera.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.filiera.dto.RichiestaEliminazioneProfiloDto;
import unicam.filiera.dto.UtenteDto;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;
import unicam.filiera.service.EliminazioneProfiloService;
import unicam.filiera.service.UtenteService;

import java.util.List;

@Controller
@RequestMapping("/gestore/richieste")
public class GestoreRichiesteEliminazioneController {

    private final UtenteService utenteService;
    private final EliminazioneProfiloService eliminazioneProfiloService;

    public GestoreRichiesteEliminazioneController(UtenteService utenteService,
                                                  EliminazioneProfiloService eliminazioneProfiloService) {
        this.utenteService = utenteService;
        this.eliminazioneProfiloService = eliminazioneProfiloService;
    }

    /**
     * Dashboard richieste eliminazione profilo (mostra quelle in attesa).
     */
    @GetMapping
    public String dashboardRichieste(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = auth.getName();

        UtenteDto gestore = utenteService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));
        model.addAttribute("utente", gestore);

        List<RichiestaEliminazioneProfiloDto> richiesteInAttesa =
                eliminazioneProfiloService.getRichiesteByStato(StatoRichiestaEliminazioneProfilo.IN_ATTESA);

        model.addAttribute("richieste", richiesteInAttesa);
        return "dashboard/richieste_eliminazione";
    }

    /**
     * Approva la richiesta: elimina l’utente e aggiorna lo stato.
     */
    @PostMapping("/approva/{id}")
    public String approvaRichiesta(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            eliminazioneProfiloService.aggiornaStato(id, StatoRichiestaEliminazioneProfilo.APPROVATA);
            ra.addFlashAttribute("successMessage", "Richiesta approvata con successo");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Errore approvazione richiesta: " + e.getMessage());
        }
        return "redirect:/gestore/richieste";
    }

    /**
     * Rifiuta la richiesta: aggiorna solo lo stato.
     */
    @PostMapping("/rifiuta/{id}")
    public String rifiutaRichiesta(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            eliminazioneProfiloService.aggiornaStato(id, StatoRichiestaEliminazioneProfilo.RIFIUTATA);
            ra.addFlashAttribute("successMessage", "Richiesta rifiutata con successo");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Errore rifiuto richiesta: " + e.getMessage());
        }
        return "redirect:/gestore/richieste";
    }

    /**
     * Endpoint API → restituisce le richieste in attesa (per AJAX o dashboard JS).
     */
    @GetMapping("/api")
    @ResponseBody
    public List<RichiestaEliminazioneProfiloDto> richiesteApi() {
        return eliminazioneProfiloService.getRichiesteByStato(StatoRichiestaEliminazioneProfilo.IN_ATTESA);
    }
}
