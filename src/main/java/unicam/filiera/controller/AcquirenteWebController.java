package unicam.filiera.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.dto.RichiestaEliminazioneProfiloDto;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.entity.RichiestaEliminazioneProfiloEntity;
import unicam.filiera.entity.UtenteEntity;
import unicam.filiera.model.Acquirente;
import unicam.filiera.model.StatoEvento;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;
import unicam.filiera.repository.*;
import unicam.filiera.service.*;
import unicam.filiera.validation.FondiValidator;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/acquirente")
public class AcquirenteWebController {

    private final UtenteRepository repo;
    private final ProdottoService prodottoService;
    private final PacchettoService pacchettoService;
    private final ProdottoTrasformatoService trasformatoService;
    private final AcquistoRepository acquistoRepository;
    private final FieraService fieraService;
    private final PrenotazioneFieraService prenotazioneFieraService;
    private final EliminazioneProfiloService eliminazioneProfiloService;
    private final RichiestaEliminazioneProfiloRepository richiestaRepo;

    public AcquirenteWebController(
            UtenteRepository repo,
            ProdottoService prodottoService,
            PacchettoService pacchettoService,
            ProdottoTrasformatoService trasformatoService,
            AcquistoRepository acquistoRepository,
            FieraService fieraService,
            PrenotazioneFieraService prenotazioneFieraService,
            EliminazioneProfiloService eliminazioneProfiloService,
            RichiestaEliminazioneProfiloRepository richiestaRepo
    ) {
        this.repo = repo;
        this.prodottoService = prodottoService;
        this.pacchettoService = pacchettoService;
        this.trasformatoService = trasformatoService;
        this.acquistoRepository = acquistoRepository;
        this.fieraService = fieraService;
        this.prenotazioneFieraService = prenotazioneFieraService;
        this.eliminazioneProfiloService = eliminazioneProfiloService;
        this.richiestaRepo = richiestaRepo;
    }

    /* ================== DASHBOARD ================== */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        String username = auth.getName();

        return repo.findById(username)
                .map(e -> {
                    Acquirente acquirente = new Acquirente(
                            e.getUsername(),
                            e.getPassword(),
                            e.getNome(),
                            e.getCognome(),
                            (e.getFondi() != null) ? e.getFondi() : 0.0
                    );
                    model.addAttribute("utente", acquirente);

                    // Prodotti approvati
                    model.addAttribute("prodotti", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));

                    // Pacchetti approvati
                    List<PacchettoDto> pacchettiDto = pacchettoService.getPacchettiByStato(StatoProdotto.APPROVATO)
                            .stream()
                            .map(p -> {
                                PacchettoDto dto = new PacchettoDto();
                                dto.setId(p.getId());
                                dto.setNome(p.getNome());
                                dto.setDescrizione(p.getDescrizione());
                                dto.setQuantita(p.getQuantita());
                                dto.setPrezzo(p.getPrezzo());
                                dto.setIndirizzo(p.getIndirizzo());
                                dto.setCreatoDa(p.getCreatoDa());
                                dto.setStato(p.getStato());
                                dto.setCommento(p.getCommento());
                                dto.setCertificatiCsv(p.getCertificatiCsv());
                                dto.setFotoCsv(p.getFotoCsv());

                                dto.setProdottiNomi(
                                        p.getProdottiIds().stream()
                                                .map(prodottoService::getProdottoById)
                                                .flatMap(Optional::stream)
                                                .map(ProdottoEntity::getNome)
                                                .toList()
                                );
                                return dto;
                            })
                            .toList();
                    model.addAttribute("pacchetti", pacchettiDto);

// ================== TRASFORMATI APPROVATI ==================
                    List<ProdottoTrasformatoDto> trasformatiDto = trasformatoService.getProdottiTrasformatiByStato(StatoProdotto.APPROVATO)
                            .stream()
                            .map(t -> {
                                ProdottoTrasformatoDto dto = new ProdottoTrasformatoDto();
                                dto.setId(t.getId());
                                dto.setNome(t.getNome());
                                dto.setDescrizione(t.getDescrizione());
                                dto.setQuantita(t.getQuantita());
                                dto.setPrezzo(t.getPrezzo());
                                dto.setIndirizzo(t.getIndirizzo());
                                dto.setCreatoDa(t.getCreatoDa());
                                dto.setStato(t.getStato());
                                dto.setCommento(t.getCommento());
                                dto.setCertificatiCsv(t.getCertificatiCsv());
                                dto.setFotoCsv(t.getFotoCsv());

                                dto.setFasiProduzione(t.getFasiProduzione());
                                return dto;
                            })
                            .toList();
                    model.addAttribute("trasformati", trasformatiDto);

                    // Acquisti utente
                    model.addAttribute("acquisti",
                            acquistoRepository.findAll()
                                    .stream()
                                    .filter(a -> a.getUsernameAcquirente().equals(username))
                                    .toList()
                    );

                    // Fiere e prenotazioni
                    model.addAttribute("fiere", fieraService.getFiereByStato(StatoEvento.PUBBLICATA));
                    model.addAttribute("prenotazioniFiere", prenotazioneFieraService.getPrenotazioniByAcquirente(username));

                    return "dashboard/acquirente";
                })
                .orElse("error/utente_non_trovato");
    }

    /* ================== aggiorna fondi ================== */
    @PostMapping("/update-fondi")
    @ResponseBody
    public ResponseEntity<?> updateFondi(@RequestBody Map<String, Double> body, Authentication auth) {
        Double importo = body.get("importo");
        if (importo == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "⚠ Importo mancante"));
        }

        try {
            FondiValidator.valida(importo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Utente non autenticato"));
        }

        String username = auth.getName();
        return repo.findById(username)
                .map(utente -> {
                    double nuoviFondi = (utente.getFondi() != null ? utente.getFondi() : 0.0) + importo;
                    utente.setFondi(nuoviFondi);
                    repo.save(utente);
                    return ResponseEntity.ok(Map.of("success", true, "nuoviFondi", nuoviFondi));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Utente non trovato")));
    }

    /* ================== eliminazione profilo ================== */
    @PostMapping("/richiesta-eliminazione")
    @ResponseBody
    public ResponseEntity<String> richiestaEliminazione(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
        }

        String loginName = auth.getName();
        UtenteEntity utente = repo.findByUsername(loginName)
                .orElseThrow(() -> new IllegalStateException("Utente non trovato"));

        try {
            RichiestaEliminazioneProfiloDto dto = RichiestaEliminazioneProfiloDto.builder()
                    .username(utente.getUsername())
                    .build();
            eliminazioneProfiloService.inviaRichiestaEliminazione(dto);
            return ResponseEntity.ok("Richiesta di eliminazione inviata con successo.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Errore: " + e.getMessage());
        }
    }

    /* ================== polling stato richiesta ================== */
    @GetMapping("/richiesta-eliminazione/stato")
    @ResponseBody
    public ResponseEntity<String> statoRichiesta(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("NON_AUTENTICATO");
        }
        String username = auth.getName();
        return richiestaRepo.findFirstByUsernameAndStatoOrderByDataRichiestaDesc(
                        username, StatoRichiestaEliminazioneProfilo.APPROVATA)
                .map(RichiestaEliminazioneProfiloEntity::getId)
                .map(id -> ResponseEntity.ok("APPROVATA:" + id))
                .orElse(ResponseEntity.ok("NESSUNA"));
    }
}
