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
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/acquirente")
public class AcquirenteWebController {

    private final UtenteRepository repo;
    private final ProdottoService prodottoService;
    private final PacchettoService pacchettoService;
    private final ProdottoTrasformatoService trasformatoService;

    public AcquirenteWebController(
            UtenteRepository repo,
            ProdottoService prodottoService,
            PacchettoService pacchettoService,
            ProdottoTrasformatoService trasformatoService
    ) {
        this.repo = repo;
        this.prodottoService = prodottoService;
        this.pacchettoService = pacchettoService;
        this.trasformatoService = trasformatoService;
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

                    // Prodotti
                    model.addAttribute("prodotti",
                            prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));

                    // Pacchetti (DTO)
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

                                dto.setCertificati(p.getCertificati() != null ? p.getCertificati() : List.of());
                                dto.setFoto(p.getFoto() != null ? p.getFoto() : List.of());

                                // Recupero nomi prodotti inclusi
                                dto.setProdottiNomi(
                                        p.getProdottiIds().stream()
                                                .map(id -> prodottoService.getProdottoById(id))
                                                .flatMap(Optional::stream)
                                                .map(ProdottoEntity::getNome)
                                                .toList()
                                );

                                return dto;
                            })
                            .toList();

                    model.addAttribute("pacchetti", pacchettiDto);

                    // Trasformati
                    model.addAttribute("trasformati",
                            trasformatoService.getProdottiTrasformatiByStato(StatoProdotto.APPROVATO));

                    return "dashboard/acquirente";
                })
                .orElse("error/utente_non_trovato");
    }
}
