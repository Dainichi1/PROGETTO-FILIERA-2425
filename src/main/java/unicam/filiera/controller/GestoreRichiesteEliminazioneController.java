package unicam.filiera.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.RichiestaEliminazioneProfiloDto;
import unicam.filiera.model.GestorePiattaforma;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;
import unicam.filiera.repository.UtenteRepository;
import unicam.filiera.service.EliminazioneProfiloService;

import java.util.List;

@Controller
@RequestMapping("/gestore/richieste")
public class GestoreRichiesteEliminazioneController {

    private final UtenteRepository utenteRepo;
    private final EliminazioneProfiloService eliminazioneProfiloService;

    public GestoreRichiesteEliminazioneController(UtenteRepository utenteRepo,
                                                  EliminazioneProfiloService eliminazioneProfiloService) {
        this.utenteRepo = utenteRepo;
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
        return utenteRepo.findById(username)
                .map(e -> {
                    GestorePiattaforma gestore = new GestorePiattaforma(
                            e.getUsername(),
                            e.getPassword(),
                            e.getNome(),
                            e.getCognome()
                    );
                    model.addAttribute("utente", gestore);

                    // Recupero richieste e le mappo in DTO
                    List<RichiestaEliminazioneProfiloDto> richiesteInAttesa =
                            eliminazioneProfiloService.getRichiesteByStato(StatoRichiestaEliminazioneProfilo.IN_ATTESA)
                                    .stream()
                                    .map(r -> new RichiestaEliminazioneProfiloDto(
                                            r.getId(),
                                            r.getUsername(),
                                            r.getStato().name(),
                                            r.getDataRichiesta()
                                    ))
                                    .toList();

                    model.addAttribute("richieste", richiesteInAttesa);

                    return "dashboard/richieste_eliminazione";
                })
                .orElse("error/utente_non_trovato");
    }

    /**
     * Approva la richiesta: elimina l’utente e aggiorna lo stato.
     */
    @PostMapping("/approva/{id}")
    public String approvaRichiesta(@PathVariable("id") Long id, Model model) {
        try {
            eliminazioneProfiloService.aggiornaStato(id, StatoRichiestaEliminazioneProfilo.APPROVATA);
        } catch (Exception e) {
            model.addAttribute("error", "Errore approvazione richiesta: " + e.getMessage());
        }
        return "redirect:/gestore/richieste";
    }

    /**
     * Rifiuta la richiesta: aggiorna solo lo stato.
     */
    @PostMapping("/rifiuta/{id}")
    public String rifiutaRichiesta(@PathVariable("id") Long id, Model model) {
        try {
            eliminazioneProfiloService.aggiornaStato(id, StatoRichiestaEliminazioneProfilo.RIFIUTATA);
        } catch (Exception e) {
            model.addAttribute("error", "Errore rifiuto richiesta: " + e.getMessage());
        }
        return "redirect:/gestore/richieste";
    }

    @GetMapping("/api")
    @ResponseBody
    public List<RichiestaEliminazioneProfiloDto> richiesteApi() {
        return eliminazioneProfiloService
                .getRichiesteByStato(StatoRichiestaEliminazioneProfilo.IN_ATTESA)
                .stream()
                .map(r -> new RichiestaEliminazioneProfiloDto(
                        r.getId(),
                        r.getUsername(),
                        r.getStato().name(),
                        r.getDataRichiesta()
                ))
                .toList();
    }
}
