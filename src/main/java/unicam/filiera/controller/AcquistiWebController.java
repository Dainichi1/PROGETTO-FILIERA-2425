package unicam.filiera.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.DatiAcquistoDto;
import unicam.filiera.dto.CartItemDto;
import unicam.filiera.model.StatoPagamento;
import unicam.filiera.service.AcquistoService;
import unicam.filiera.service.PagamentoService;
import unicam.filiera.service.CarrelloService;
import unicam.filiera.validation.AcquistoValidator;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/acquirente")
public class AcquistiWebController {

    private static final Logger log = LoggerFactory.getLogger(AcquistiWebController.class);

    private final AcquistoService acquistoService;
    private final PagamentoService pagamentoService;
    private final CarrelloService carrelloService;   // inietto CarrelloService
    private final ObjectMapper objectMapper;

    public AcquistiWebController(AcquistoService acquistoService,
                                 PagamentoService pagamentoService,
                                 CarrelloService carrelloService,
                                 ObjectMapper objectMapper) {
        this.acquistoService = acquistoService;
        this.pagamentoService = pagamentoService;
        this.carrelloService = carrelloService;
        this.objectMapper = objectMapper;
    }

    // ================== conferma acquisto ==================
    @PostMapping("/conferma-acquisto")
    @ResponseBody
    public ResponseEntity<?> confermaAcquisto(@RequestBody DatiAcquistoDto dto,
                                              Authentication auth,
                                              HttpSession session) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Utente non autenticato"
            ));
        }

        try {
            dto.setUsernameAcquirente(auth.getName());

            log.debug("📦 Payload JSON ricevuto: {}", objectMapper.writeValueAsString(dto));
            log.info(">>> Utente [{}] conferma acquisto con metodo [{}]",
                    dto.getUsernameAcquirente(), dto.getTipoMetodoPagamento());

            // Validazione
            AcquistoValidator.valida(dto);

            // Pagamento
            StatoPagamento esito = pagamentoService.effettuaPagamento(dto);
            dto.setStatoPagamento(esito);

            log.info(">>> Esito pagamento [{}] per utente [{}]", esito, dto.getUsernameAcquirente());

            if (esito == StatoPagamento.APPROVATO) {
                acquistoService.salvaAcquisto(dto);

                // 🛒 Rimuovo solo gli item acquistati dal carrello (non tutto)
                if (dto.getItems() != null) {
                    dto.getItems().forEach(item -> {
                        try {
                            carrelloService.rimuoviItem(item.getTipo(), item.getId(), session);
                            log.debug("🛒 Rimosso item acquistato: {} - {}", item.getTipo(), item.getId());
                        } catch (Exception e) {
                            log.warn("⚠ Errore durante rimozione item {} dal carrello", item.getId(), e);
                        }
                    });
                }

                List<CartItemDto> carrelloAggiornato = carrelloService.getItems(session);

                log.info("✅ Acquisto completato per utente [{}], carrello aggiornato", dto.getUsernameAcquirente());

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Acquisto completato con successo!",
                        "clearCart", false, // 👉 NON svuoto tutto
                        "items", carrelloAggiornato
                ));
            } else {
                log.warn("❌ Pagamento rifiutato per utente [{}]", dto.getUsernameAcquirente());
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Pagamento non riuscito",
                        "clearCart", false
                ));
            }
        } catch (IllegalArgumentException ex) {
            log.warn("⚠ Errore di validazione acquisto: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", ex.getMessage(),
                    "clearCart", false
            ));
        } catch (Exception ex) {
            log.error("❌ Errore interno durante l'acquisto per utente {}", auth.getName(), ex);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Errore interno durante l'acquisto: " + ex.getMessage(),
                    "clearCart", false
            ));
        }
    }

    // ================== lista acquisti utente ==================
    @GetMapping("/storico-acquisti")
    public String storicoAcquisti(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = auth.getName();
        model.addAttribute("acquisti", acquistoService.getAcquistiByUsername(username));
        return "dashboard/storico_acquisti";
    }

    // ================== dettaglio acquisto ==================
    @GetMapping("/acquisto/{id}")
    @ResponseBody
    public ResponseEntity<?> dettaglioAcquisto(@PathVariable Long id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Utente non autenticato"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "items", acquistoService.getItemsByAcquisto(id)
        ));
    }
}
