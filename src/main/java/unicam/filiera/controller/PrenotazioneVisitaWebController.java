package unicam.filiera.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.EsitoPrenotazioneVisitaDto;
import unicam.filiera.dto.PrenotazioneVisitaDto;
import unicam.filiera.service.PrenotazioneVisitaService;

@RestController
@RequestMapping("/prenotazioni-visite")
public class PrenotazioneVisitaWebController {

    private final PrenotazioneVisitaService prenotazioneVisitaService;

    @Autowired
    public PrenotazioneVisitaWebController(PrenotazioneVisitaService prenotazioneVisitaService) {
        this.prenotazioneVisitaService = prenotazioneVisitaService;
    }

    // ===================== CREA =====================
    @PostMapping("/prenota")
    public ResponseEntity<EsitoPrenotazioneVisitaDto> prenota(@Valid @ModelAttribute PrenotazioneVisitaDto dto,
                                                              Authentication auth) {
        String username = (auth != null) ? auth.getName() : "demo_user";

        try {
            prenotazioneVisitaService.creaPrenotazione(dto, username);
            return ResponseEntity.status(201).body(
                    new EsitoPrenotazioneVisitaDto(true, "✅ Prenotazione effettuata con successo!")
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(
                    new EsitoPrenotazioneVisitaDto(false, "⚠ Hai già prenotato questa visita.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new EsitoPrenotazioneVisitaDto(false, e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new EsitoPrenotazioneVisitaDto(false, "❌ Errore imprevisto: " + e.getMessage())
            );
        }
    }

    // ===================== ELIMINA =====================
    @DeleteMapping("/{id}")
    public ResponseEntity<EsitoPrenotazioneVisitaDto> elimina(@PathVariable Long id, Authentication auth) {
        String username = (auth != null) ? auth.getName() : "demo_user";

        try {
            prenotazioneVisitaService.eliminaById(id, username);
            return ResponseEntity.ok(
                    new EsitoPrenotazioneVisitaDto(true, "✅ Prenotazione eliminata con successo!")
            );
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(
                    new EsitoPrenotazioneVisitaDto(false, "⚠ Non sei autorizzato a eliminare questa prenotazione.")
            );
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(404).body(
                    new EsitoPrenotazioneVisitaDto(false, "⚠ Prenotazione non trovata.")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new EsitoPrenotazioneVisitaDto(false, "❌ Errore durante l'eliminazione: " + e.getMessage())
            );
        }
    }
}
