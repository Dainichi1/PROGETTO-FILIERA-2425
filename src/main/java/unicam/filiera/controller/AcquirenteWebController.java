package unicam.filiera.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import unicam.filiera.dto.PacchettoViewDto;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.model.Acquirente;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.repository.UtenteRepository;
import unicam.filiera.service.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/acquirente")
public class AcquirenteWebController {

    private final UtenteRepository repo;
    private final ProdottoService prodottoService;
    private final PacchettoService pacchettoService;
    private final ProdottoTrasformatoService trasformatoService;
    private final FieraService fieraService;

    public AcquirenteWebController(
            UtenteRepository repo,
            ProdottoService prodottoService,
            PacchettoService pacchettoService,
            ProdottoTrasformatoService trasformatoService,
            FieraService fieraService
    ) {
        this.repo = repo;
        this.prodottoService = prodottoService;
        this.pacchettoService = pacchettoService;
        this.trasformatoService = trasformatoService;
        this.fieraService = fieraService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = auth.getName();

        return repo.findById(username)
                .map(e -> {
                    // Creazione Acquirente
                    Acquirente acquirente = new Acquirente(
                            e.getUsername(),
                            e.getPassword(),
                            e.getNome(),
                            e.getCognome(),
                            (e.getFondi() != null) ? e.getFondi() : 0.0
                    );

                    model.addAttribute("utente", acquirente);

                    // ✅ Prodotti
                    model.addAttribute("prodotti", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));

                    // ✅ Pacchetti (conversione in DTO con prodottiNomi)
                    List<PacchettoViewDto> pacchettiDto = pacchettoService.getPacchettiByStato(StatoProdotto.APPROVATO)
                            .stream()
                            .map(p -> {
                                PacchettoViewDto dto = new PacchettoViewDto();
                                dto.setId(p.getId());
                                dto.setNome(p.getNome());
                                dto.setDescrizione(p.getDescrizione());
                                dto.setQuantita(p.getQuantita());
                                dto.setPrezzo(p.getPrezzo());
                                dto.setIndirizzo(p.getIndirizzo());
                                dto.setCreatoDa(p.getCreatoDa());
                                dto.setStato(p.getStato().name());
                                dto.setCommento(p.getCommento());

                                // certificati e foto sono già liste
                                dto.setCertificati(p.getCertificati() != null ? p.getCertificati() : List.of());
                                dto.setFoto(p.getFoto() != null ? p.getFoto() : List.of());

                                // recupera i nomi dei prodotti inclusi dal service
                                List<String> prodottiNomi = p.getProdottiIds().stream()
                                        .map(id -> prodottoService.getProdottoById(id).orElse(null)) // Optional -> Entity/null
                                        .filter(Objects::nonNull)
                                        .map(ProdottoEntity::getNome)
                                        .collect(Collectors.toList());

                                dto.setProdottiNomi(prodottiNomi);

                                return dto;
                            })
                            .toList();

                    model.addAttribute("pacchetti", pacchettiDto);

                    // ✅ Trasformati
                    model.addAttribute("trasformati", trasformatoService.getProdottiTrasformatiByStato(StatoProdotto.APPROVATO));

                    // ✅ Fiere
                    model.addAttribute("fiere", fieraService.getFierePubblicate());

                    return "dashboard/acquirente";
                })
                .orElse("error/utente_non_trovato");
    }
}
