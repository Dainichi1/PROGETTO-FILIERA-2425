package unicam.filiera.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.filiera.dto.ItemTipo;
import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.service.ProdottoService;
import unicam.filiera.service.ProdottoTrasformatoService;
import unicam.filiera.service.UtenteService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/trasformatore")
public class TrasformatoreWebController {

    private static final Logger log = LoggerFactory.getLogger(TrasformatoreWebController.class);

    private final ProdottoTrasformatoService trasformatoService;
    private final ProdottoService prodottoService;
    private final UtenteService utenteService;

    @Autowired
    public TrasformatoreWebController(ProdottoTrasformatoService trasformatoService,
                                      ProdottoService prodottoService,
                                      UtenteService utenteService) {
        this.trasformatoService = trasformatoService;
        this.prodottoService = prodottoService;
        this.utenteService = utenteService;
    }

    @ModelAttribute("trasformatoDto")
    public ProdottoTrasformatoDto trasformatoDto() {
        ProdottoTrasformatoDto d = new ProdottoTrasformatoDto();
        d.setTipo(ItemTipo.TRASFORMATO);
        return d;
    }


    @GetMapping("/dashboard")
    public String dashboardTrasformatore(Model model, Authentication authentication) {
        if (!model.containsAttribute("trasformatoDto")) {
            model.addAttribute("trasformatoDto", new ProdottoTrasformatoDto());
        }

        String username = (authentication != null) ? authentication.getName() : "trasformatore_demo";
        model.addAttribute("trasformati", trasformatoService.getProdottiTrasformatiCreatiDa(username));
        model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
        model.addAttribute("produttori", utenteService.getProduttori());
        model.addAttribute("showForm", false);

        return "dashboard/trasformatore";
    }

    @GetMapping("/prodotti/{usernameProduttore}")
    @ResponseBody
    public List<Map<String, String>> getProdottiApprovatiByProduttore(@PathVariable String usernameProduttore) {
        return prodottoService.getProdottiApprovatiByProduttore(usernameProduttore)
                .stream()
                .map(p -> Map.of("id", String.valueOf(p.getId()), "nome", p.getNome()))
                .toList();
    }

    @PostMapping("/crea")
    public String creaProdottoTrasformato(
            @Valid @ModelAttribute("trasformatoDto") ProdottoTrasformatoDto trasformatoDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttrs,
            Model model
    ) {
        String username = (authentication != null) ? authentication.getName() : "trasformatore_demo";

        // Validazione centralizzata (aggiornata)
        validaTrasformato(trasformatoDto, bindingResult);

        if (bindingResult.hasErrors()) {
            log.warn("Creazione prodotto trasformato fallita per errori di validazione");

            model.addAttribute("trasformati", trasformatoService.getProdottiTrasformatiCreatiDa(username));
            model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
            model.addAttribute("produttori", utenteService.getProduttori());
            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);

            return "dashboard/trasformatore";
        }

        try {
            trasformatoService.creaProdottoTrasformato(trasformatoDto, username);
            redirectAttrs.addFlashAttribute("createSuccessMessage", "Prodotto trasformato inviato al Curatore con successo");
            return "redirect:/trasformatore/dashboard";

        } catch (Exception ex) {
            log.error("Errore nella creazione del prodotto trasformato", ex);

            model.addAttribute("trasformati", trasformatoService.getProdottiTrasformatiCreatiDa(username));
            model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
            model.addAttribute("produttori", utenteService.getProduttori());
            model.addAttribute("errorMessage", "Errore: " + ex.getMessage());
            model.addAttribute("showForm", true);

            return "dashboard/trasformatore";
        }
    }

    @DeleteMapping("/elimina/{id}")
    @ResponseBody
    public ResponseEntity<String> eliminaTrasformato(@PathVariable Long id, Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "trasformatore_demo";

        try {
            trasformatoService.eliminaProdottoTrasformatoById(id, username);
            return ResponseEntity.ok("Prodotto trasformato eliminato con successo");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            log.error("Errore durante eliminazione prodotto trasformato", e);
            return ResponseEntity.status(500).body("Errore interno durante l'eliminazione");
        }
    }

    // === Endpoint JSON per pre-popolare il form in modifica ===
    @GetMapping("/api/trasformato/{id}")
    @ResponseBody
    public ResponseEntity<?> getTrasformato(@PathVariable Long id, Authentication authentication) {
        String username = (authentication != null) ? authentication.getName() : "trasformatore_demo";

        return trasformatoService.findEntityById(id)
                .map(entity -> {
                    if (!entity.getCreatoDa().equals(username)) {
                        return ResponseEntity.status(403).body("Non autorizzato");
                    }

                    var view = new TrasformatoView(
                            entity.getId(),
                            entity.getNome(),
                            entity.getDescrizione(),
                            entity.getQuantita(),
                            entity.getPrezzo(),
                            entity.getIndirizzo(),
                            entity.getStato().name(),
                            entity.getCommento(),
                            (entity.getCertificati() == null || entity.getCertificati().isBlank())
                                    ? java.util.List.of()
                                    : java.util.List.of(entity.getCertificati().split(",")),
                            (entity.getFoto() == null || entity.getFoto().isBlank())
                                    ? java.util.List.of()
                                    : java.util.List.of(entity.getFoto().split(",")),
                            entity.getFasiProduzione() == null
                                    ? java.util.List.of()
                                    : entity.getFasiProduzione().stream()
                                    .map(f -> new FaseView(f.getDescrizioneFase(),
                                            f.getProduttoreUsername(),
                                            f.getProdottoOrigineId()))
                                    .toList()
                    );

                    return ResponseEntity.ok(view);
                })
                .orElseGet(() -> ResponseEntity.status(404).body("Prodotto trasformato non trovato"));
    }

    // --- Validazione centralizzata (AGGIORNATA: conta solo fasi valide) ---
    private void validaTrasformato(ProdottoTrasformatoDto dto, BindingResult br) {
        boolean isCreazione = (dto.getId() == null);

        if (isCreazione) {
            if (dto.getCertificati() == null || dto.getCertificati().isEmpty()
                    || dto.getCertificati().stream().allMatch(MultipartFile::isEmpty)) {
                br.rejectValue("certificati", "error.certificati", "Devi caricare almeno un certificato");
            }
            if (dto.getFoto() == null || dto.getFoto().isEmpty()
                    || dto.getFoto().stream().allMatch(MultipartFile::isEmpty)) {
                br.rejectValue("foto", "error.foto", "Devi caricare almeno una foto");
            }
        }

        // TIPI ESPLICITI: niente più Object
        List<ProdottoTrasformatoDto.FaseProduzioneDto> fasi =
                (dto.getFasiProduzione() == null)
                        ? Collections.emptyList()
                        : dto.getFasiProduzione();

        long fasiValide = fasi.stream()
                .filter(f -> f != null
                        && f.getDescrizioneFase() != null && !f.getDescrizioneFase().isBlank()
                        && f.getProduttoreUsername() != null && !f.getProduttoreUsername().isBlank()
                        && f.getProdottoOrigineId() != null)
                .count();

        if (fasiValide < 2) {
            br.rejectValue("fasiProduzione", "error.fasiProduzione", "Devi inserire almeno 2 fasi di produzione");
        }
    }

    // === DTO annidati statici per risposte JSON ===
    public record FaseView(String descrizioneFase, String produttoreUsername, Long prodottoOrigineId) {}
    public record TrasformatoView(
            Long id,
            String nome,
            String descrizione,
            Integer quantita,
            Double prezzo,
            String indirizzo,
            String stato,
            String commento,
            java.util.List<String> certificati,
            java.util.List<String> foto,
            java.util.List<FaseView> fasiProduzione
    ) {}
}
