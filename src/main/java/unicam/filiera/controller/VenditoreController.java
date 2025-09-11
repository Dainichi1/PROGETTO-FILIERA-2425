package unicam.filiera.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.filiera.dto.*;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.service.PacchettoService;
import unicam.filiera.service.ProdottoService;
import unicam.filiera.service.ProdottoTrasformatoService;


/**
 * Controller di supporto per le operazioni centralizzate sugli {@link unicam.filiera.dto.BaseItemDto},
 * usato principalmente dal frontend (fetch JSON e update).
 *
 * Responsabilità principali:
 *
 * Espone endpoint di fetch centralizzato per recuperare i dati di un item
 * in base a ID e {@link unicam.filiera.dto.ItemTipo}.
 * Gestisce la <b>modifica centralizzata di item, delegando alle service layer
 * corrispondenti ({@code ProdottoService}, {@code PacchettoService}, {@code ProdottoTrasformatoService}).
 * Gestisce l’eliminazione centralizzata con risposta JSON (successo/errore).
 *
 *
 * Nota: questo controller ha finalità frontend/API,
 * mentre {@link ItemController} si occupa delle validazioni di business lato server
 * per gli item rifiutati o non approvati.
 */
@Controller
@RequestMapping("/venditore/item")
public class VenditoreController {

    private final ProdottoService prodottoService;
    private final PacchettoService pacchettoService;
    private final ProdottoTrasformatoService trasformatoService;

    @Autowired
    public VenditoreController(ProdottoService prodottoService,
                               PacchettoService pacchettoService,
                               ProdottoTrasformatoService trasformatoService) {
        this.prodottoService = prodottoService;
        this.pacchettoService = pacchettoService;
        this.trasformatoService = trasformatoService;
    }

    private String resolveUsername(Authentication auth) {
        return (auth != null) ? auth.getName() : "demo_user";
    }

    // Factory per DTO corretta
    @ModelAttribute("item")
    public BaseItemDto createDto(@RequestParam("tipo") ItemTipo tipo) {
        return switch (tipo) {
            case PRODOTTO -> new ProdottoDto();
            case PACCHETTO -> new PacchettoDto();
            case TRASFORMATO -> new ProdottoTrasformatoDto();
        };
    }

    // ========== FETCH ==========
    @GetMapping("/fetch/{id}")
    @ResponseBody
    public ResponseEntity<?> fetchItem(@PathVariable Long id,
                                       @RequestParam("tipo") ItemTipo tipo) {
        return switch (tipo) {
            case PRODOTTO -> prodottoService.findEntityById(id)
                    .map(entity -> {
                        ProdottoDto dto = new ProdottoDto();
                        dto.setId(entity.getId());
                        dto.setNome(entity.getNome());
                        dto.setDescrizione(entity.getDescrizione());
                        dto.setQuantita(entity.getQuantita());
                        dto.setPrezzo(entity.getPrezzo());
                        dto.setIndirizzo(entity.getIndirizzo());
                        return ResponseEntity.ok(dto);
                    })
                    .orElse(ResponseEntity.notFound().build());

            case PACCHETTO -> pacchettoService.findEntityById(id)
                    .map(entity -> {
                        PacchettoDto dto = new PacchettoDto();
                        dto.setId(entity.getId());
                        dto.setNome(entity.getNome());
                        dto.setDescrizione(entity.getDescrizione());
                        dto.setQuantita(entity.getQuantita());
                        dto.setPrezzo(entity.getPrezzo());
                        dto.setIndirizzo(entity.getIndirizzo());
                        if (entity.getProdotti() != null) {
                            dto.setProdottiSelezionati(
                                    entity.getProdotti().stream()
                                            .map(ProdottoEntity::getId)
                                            .toList()
                            );
                        }
                        return ResponseEntity.ok(dto);
                    })
                    .orElse(ResponseEntity.notFound().build());

            case TRASFORMATO -> trasformatoService.findEntityById(id)
                    .map(entity -> {
                        ProdottoTrasformatoDto dto = new ProdottoTrasformatoDto();
                        dto.setId(entity.getId());
                        dto.setNome(entity.getNome());
                        dto.setDescrizione(entity.getDescrizione());
                        dto.setQuantita(entity.getQuantita());
                        dto.setPrezzo(entity.getPrezzo());
                        dto.setIndirizzo(entity.getIndirizzo());
                        if (entity.getFasiProduzione() != null) {
                            dto.setFasiProduzione(
                                    entity.getFasiProduzione().stream().map(f -> {
                                        var faseDto = new ProdottoTrasformatoDto.FaseProduzioneDto();
                                        faseDto.setDescrizioneFase(f.getDescrizioneFase());
                                        faseDto.setProduttoreUsername(f.getProduttoreUsername());
                                        faseDto.setProdottoOrigineId(f.getProdottoOrigineId());
                                        return faseDto;
                                    }).toList()
                            );
                        }
                        return ResponseEntity.ok(dto);
                    })
                    .orElse(ResponseEntity.notFound().build());
        };
    }

    // ========== MODIFICA NORMALE ==========
    @PostMapping("/modifica")
    public String modificaItem(@Valid @ModelAttribute("item") BaseItemDto dto,
                               Authentication auth,
                               RedirectAttributes ra) {
        String username = resolveUsername(auth);

        try {
            switch (dto.getTipo()) {
                case PRODOTTO -> {
                    prodottoService.aggiornaProdotto(dto.getId(), (ProdottoDto) dto, username);
                    ra.addFlashAttribute("updateSuccessMessage", "Prodotto aggiornato con successo");
                    return "redirect:/produttore/dashboard";
                }
                case PACCHETTO -> {
                    pacchettoService.aggiornaPacchetto(dto.getId(), (PacchettoDto) dto, username);
                    ra.addFlashAttribute("updateSuccessMessage", "Pacchetto aggiornato con successo");
                    return "redirect:/distributore/dashboard";
                }
                case TRASFORMATO -> {
                    trasformatoService.aggiornaProdottoTrasformato(
                            dto.getId(),
                            (ProdottoTrasformatoDto) dto,
                            username
                    );
                    ra.addFlashAttribute("updateSuccessMessage", "Prodotto trasformato aggiornato con successo");
                    return "redirect:/trasformatore/dashboard";
                }
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Errore durante la modifica: " + e.getMessage());
        }

        return "redirect:/";
    }

    // ========== ELIMINA ==========
    @DeleteMapping("/elimina/{id}")
    @ResponseBody
    public ResponseEntity<String> eliminaItem(@PathVariable Long id,
                                              @RequestParam("tipo") ItemTipo tipo,
                                              Authentication auth) {
        String username = resolveUsername(auth);
        try {
            switch (tipo) {
                case PRODOTTO -> prodottoService.eliminaById(id, username);
                case PACCHETTO -> pacchettoService.eliminaById(id, username);
                case TRASFORMATO -> trasformatoService.eliminaById(id, username);
            }
            return ResponseEntity.ok("Elemento eliminato con successo");
        } catch (SecurityException se) {
            return ResponseEntity.status(403).body(se.getMessage());
        } catch (IllegalStateException ise) {
            return ResponseEntity.badRequest().body(ise.getMessage());
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Errore durante l'eliminazione: " + e.getMessage());
        }
    }
}
