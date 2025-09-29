package unicam.filiera.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.EsitoPrenotazioneDto;
import unicam.filiera.dto.PrenotazioneFieraDto;
import unicam.filiera.service.PrenotazioneFieraService;

@RestController
@RequestMapping("/prenotazioni-fiere")
public class PrenotazioneFieraWebController {

    private final PrenotazioneFieraService prenotazioneFieraService;

    @Autowired
    public PrenotazioneFieraWebController(PrenotazioneFieraService prenotazioneFieraService) {
        this.prenotazioneFieraService = prenotazioneFieraService;
    }

    // ===================== CREA =====================
    @PostMapping("/prenota")
    public ResponseEntity<EsitoPrenotazioneDto> prenota(@Valid @ModelAttribute PrenotazioneFieraDto dto,
                                                        Authentication auth) {
        String username = (auth != null) ? auth.getName() : "demo_user";

        try {
            double nuoviFondi = prenotazioneFieraService.creaPrenotazione(dto, username);

            return ResponseEntity.status(201).body(
                    new EsitoPrenotazioneDto(true, "✅ Prenotazione alla fiera effettuata con successo!", nuoviFondi)
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    new EsitoPrenotazioneDto(false, e.getMessage(), null)
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new EsitoPrenotazioneDto(false, "❌ Errore imprevisto: " + e.getMessage(), null)
            );
        }
    }

    // ===================== ELIMINA =====================
    @DeleteMapping("/{id}")
    public ResponseEntity<EsitoPrenotazioneDto> elimina(@PathVariable Long id, Authentication auth) {
        String username = (auth != null) ? auth.getName() : "demo_user";

        try {
            double nuoviFondi = prenotazioneFieraService.eliminaById(id, username);

            return ResponseEntity.ok(
                    new EsitoPrenotazioneDto(true, "✅ Prenotazione alla fiera eliminata con successo!", nuoviFondi)
            );

        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(
                    new EsitoPrenotazioneDto(false, "⚠ Non sei autorizzato a eliminare questa prenotazione.", null)
            );

        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(404).body(
                    new EsitoPrenotazioneDto(false, "⚠ Prenotazione non trovata.", null)
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new EsitoPrenotazioneDto(false, "❌ Errore durante l’eliminazione: " + e.getMessage(), null)
            );
        }
    }
}
