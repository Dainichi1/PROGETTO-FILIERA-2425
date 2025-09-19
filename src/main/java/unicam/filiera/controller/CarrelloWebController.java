package unicam.filiera.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.*;
import unicam.filiera.model.Item;
import unicam.filiera.service.CarrelloService;
import unicam.filiera.validation.CarrelloValidator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/carrello")
public class CarrelloWebController {

    private final CarrelloService carrelloService;

    public CarrelloWebController(CarrelloService carrelloService) {
        this.carrelloService = carrelloService;
    }

    // ======================
    // Aggiungi item al carrello
    // ======================
    @PostMapping("/aggiungi")
    public ResponseEntity<Map<String, Object>> addItemToCart(
            @Valid @RequestBody AddToCartRequestDto request,
            HttpSession session
    ) {
        Map<String, Object> resp = new HashMap<>();
        try {
            CarrelloValidator.valida(request);
            carrelloService.aggiungiItem(request.getTipo(), request.getId(), request.getQuantita(), session);

            List<CartItemDto> items = carrelloService.getItems(session);
            CartTotalsDto totali = carrelloService.calcolaTotali(session);

            resp.put("success", true);
            resp.put("items", items);
            resp.put("totali", totali);

            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            resp.put("success", false);
            resp.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(resp);
        }
    }

    // ======================
    // Aggiorna quantità item (se 0 → rimuove e ritorna disponibilità aggiornata)
    // ======================
    @PostMapping("/aggiorna")
    public ResponseEntity<Map<String, Object>> aggiornaItem(
            @RequestBody Map<String, Object> payload,
            HttpSession session
    ) {
        Map<String, Object> resp = new HashMap<>();
        try {
            ItemTipo tipo = ItemTipo.valueOf(((String) payload.get("tipo")).toUpperCase());
            Long id = ((Number) payload.get("id")).longValue();
            int nuovaQuantita = ((Number) payload.get("nuovaQuantita")).intValue();

            carrelloService.aggiornaQuantitaItem(tipo, id, nuovaQuantita, session);

            List<CartItemDto> items = carrelloService.getItems(session);
            CartTotalsDto totali = carrelloService.calcolaTotali(session);

            resp.put("success", true);
            resp.put("items", items);
            resp.put("totali", totali);

            // ✅ se l’item è stato eliminato (nuovaQuantita = 0), ritorna disponibilità aggiornata dal DB
            if (nuovaQuantita == 0) {
                Item removedItem = carrelloService.getItemFromDb(tipo, id);
                resp.put("removedItem", Map.of(
                        "id", removedItem.getId(),
                        "tipo", tipo.toString(),
                        "disponibilita", removedItem.getQuantita()
                ));
            }

            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            resp.put("success", false);
            resp.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(resp);
        }
    }

    // ======================
    // Rimuovi item dal carrello
    // ======================
    @PostMapping("/rimuovi")
    public ResponseEntity<Map<String, Object>> rimuoviItem(
            @RequestBody Map<String, Object> payload,
            HttpSession session
    ) {
        Map<String, Object> resp = new HashMap<>();
        try {
            ItemTipo tipo = ItemTipo.valueOf(((String) payload.get("tipo")).toUpperCase());
            Long id = ((Number) payload.get("id")).longValue();

            carrelloService.rimuoviItem(tipo, id, session);

            List<CartItemDto> items = carrelloService.getItems(session);
            CartTotalsDto totali = carrelloService.calcolaTotali(session);

            // ✅ ritorno anche la disponibilità aggiornata per il marketplace
            Item removedItem = carrelloService.getItemFromDb(tipo, id);

            resp.put("success", true);
            resp.put("items", items);
            resp.put("totali", totali);
            resp.put("removedItem", Map.of(
                    "id", removedItem.getId(),
                    "tipo", tipo.toString(),
                    "disponibilita", removedItem.getQuantita()
            ));

            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            resp.put("success", false);
            resp.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(resp);
        }
    }

    // ======================
    // Ottieni il carrello corrente
    // ======================
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(HttpSession session) {
        List<CartItemDto> items = carrelloService.getItems(session);
        CartTotalsDto totali = carrelloService.calcolaTotali(session);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("items", items);
        resp.put("totali", totali);

        return ResponseEntity.ok(resp);
    }

    // ======================
    // Svuota il carrello
    // ======================
    @PostMapping("/svuota")
    public ResponseEntity<Map<String, Object>> clearCart(HttpSession session) {
        carrelloService.svuota(session);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("items", List.of());
        resp.put("totali", new CartTotalsDto(0, 0.0));

        return ResponseEntity.ok(resp);
    }
}
