package unicam.filiera.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.PrenotazioneVisitaDto;
import unicam.filiera.service.PrenotazioneVisitaService;

@Controller
@RequestMapping("/prenotazioni-visite")
public class PrenotazioneVisitaWebController {

    private final PrenotazioneVisitaService prenotazioneVisitaService;

    @Autowired
    public PrenotazioneVisitaWebController(PrenotazioneVisitaService prenotazioneVisitaService) {
        this.prenotazioneVisitaService = prenotazioneVisitaService;
    }

    @PostMapping("/prenota")
    @ResponseBody
    public ResponseEntity<String> prenota(@Valid @ModelAttribute PrenotazioneVisitaDto dto,
                                          Authentication auth) {
        String username = (auth != null) ? auth.getName() : "demo_user";

        try {
            prenotazioneVisitaService.creaPrenotazione(dto, username);
            return ResponseEntity.ok("✅ Prenotazione effettuata con successo!");
        } catch (IllegalStateException e) {
            // Caso: già prenotato
            return ResponseEntity.status(409).body("⚠ Hai già prenotato questa visita.");
        } catch (IllegalArgumentException e) {
            // Caso: errore validazione lato server
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("❌ Errore imprevisto: " + e.getMessage());
        }
    }
}
