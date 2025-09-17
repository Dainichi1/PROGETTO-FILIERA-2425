package unicam.filiera.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.AddToCartRequestDto;
import unicam.filiera.dto.CartItemDto;
import unicam.filiera.dto.CartTotalsDto;
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

            // aggiungi item nella sessione
            carrelloService.aggiungiItem(request.getTipo(), request.getId(), request.getQuantita(), session);

            List<CartItemDto> items = carrelloService.getItems(session);
            CartTotalsDto totali = carrelloService.calcolaTotali(session);

// prende l’ultimo aggiunto
            String nomeItem = items.isEmpty() ? "Item" : items.get(items.size() - 1).getNome();

            resp.put("success", true);
            resp.put("message", nomeItem + " aggiunto al carrello");
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
    // Ottieni il carrello corrente
    // ======================
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        List<CartItemDto> items = carrelloService.getItems(session);
        CartTotalsDto totali = carrelloService.calcolaTotali(session);

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
        resp.put("message", "Carrello svuotato");
        resp.put("items", List.of());
        resp.put("totali", new CartTotalsDto(0, 0.0));

        return ResponseEntity.ok(resp);
    }
}
