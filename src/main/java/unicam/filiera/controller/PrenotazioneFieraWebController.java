package unicam.filiera.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.PrenotazioneFieraDto;
import unicam.filiera.service.PrenotazioneFieraService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/prenotazioni-fiere")
public class PrenotazioneFieraWebController {

    private final PrenotazioneFieraService prenotazioneFieraService;

    @Autowired
    public PrenotazioneFieraWebController(PrenotazioneFieraService prenotazioneFieraService) {
        this.prenotazioneFieraService = prenotazioneFieraService;
    }

    // ===================== CREA =====================
    @PostMapping("/prenota")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> prenota(@Valid @ModelAttribute PrenotazioneFieraDto dto,
                                                       Authentication auth) {
        String username = (auth != null) ? auth.getName() : "demo_user";
        Map<String, Object> response = new HashMap<>();

        try {
            double nuoviFondi = prenotazioneFieraService.creaPrenotazione(dto, username);

            response.put("success", true);
            response.put("message", "✅ Prenotazione alla fiera effettuata con successo!");
            response.put("nuoviFondi", nuoviFondi);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Include sia fondi insufficienti che errori di validazione
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "❌ Errore imprevisto: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ===================== ELIMINA =====================
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> elimina(@PathVariable Long id, Authentication auth) {
        String username = (auth != null) ? auth.getName() : "demo_user";
        Map<String, Object> response = new HashMap<>();

        try {
            double nuoviFondi = prenotazioneFieraService.eliminaById(id, username);

            response.put("success", true);
            response.put("message", "✅ Prenotazione alla fiera eliminata con successo!");
            response.put("nuoviFondi", nuoviFondi);

            return ResponseEntity.ok(response);
        } catch (SecurityException se) {
            response.put("success", false);
            response.put("message", "⚠ Non sei autorizzato a eliminare questa prenotazione.");
            return ResponseEntity.status(403).body(response);
        } catch (IllegalArgumentException iae) {
            response.put("success", false);
            response.put("message", "⚠ Prenotazione non trovata.");
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "❌ Errore durante l’eliminazione: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
