package unicam.filiera.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.PacchettoViewDto;
import unicam.filiera.entity.*;
import unicam.filiera.repository.*;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.StatoEvento;

import java.util.List;

@Controller
@RequestMapping("/marketplace")
public class MarketplaceWebController {

    private final ProdottoRepository prodottoRepository;
    private final PacchettoRepository pacchettoRepository;
    private final ProdottoTrasformatoRepository prodottoTrasformatoRepository;
    private final VisitaInvitoRepository visitaInvitoRepository;
    private final FieraRepository fieraRepository;

    public MarketplaceWebController(ProdottoRepository prodottoRepository,
                                    PacchettoRepository pacchettoRepository,
                                    ProdottoTrasformatoRepository prodottoTrasformatoRepository,
                                    VisitaInvitoRepository visitaInvitoRepository, FieraRepository fieraRepository) {
        this.prodottoRepository = prodottoRepository;
        this.pacchettoRepository = pacchettoRepository;
        this.prodottoTrasformatoRepository = prodottoTrasformatoRepository;
        this.visitaInvitoRepository = visitaInvitoRepository;
        this.fieraRepository = fieraRepository;
    }

    @GetMapping
    public String mostraMarketplace(Model model) {
        List<ProdottoEntity> prodotti = prodottoRepository.findByStato(StatoProdotto.APPROVATO);
        List<PacchettoEntity> pacchetti = pacchettoRepository.findByStato(StatoProdotto.APPROVATO);
        List<ProdottoTrasformatoEntity> trasformati = prodottoTrasformatoRepository.findByStato(StatoProdotto.APPROVATO);
        List<VisitaInvitoEntity> visite = visitaInvitoRepository.findByStato(StatoEvento.PUBBLICATA);
        List<FieraEntity> fiere = fieraRepository.findByStato(StatoEvento.PUBBLICATA);

        model.addAttribute("prodotti", prodotti);
        model.addAttribute("pacchetti", pacchetti);
        model.addAttribute("trasformati", trasformati);
        model.addAttribute("visite", visite);
        model.addAttribute("fiere", fiere);

        return "marketplace/lista";
    }

    @GetMapping("/prodotto/{id}")
    public String mostraDettagliProdotto(@PathVariable Long id, Model model) {
        ProdottoEntity prodotto = prodottoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto non trovato"));
        model.addAttribute("elemento", prodotto);
        model.addAttribute("tipo", "prodotto");
        return "marketplace/dettagli";
    }

    @GetMapping("/pacchetto/{id}")
    public String mostraDettagliPacchetto(@PathVariable Long id, Model model) {
        PacchettoEntity pacchetto = pacchettoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto non trovato"));

        // converto inline in DTO
        PacchettoViewDto dto = new PacchettoViewDto();
        dto.setId(pacchetto.getId());
        dto.setNome(pacchetto.getNome());
        dto.setDescrizione(pacchetto.getDescrizione());
        dto.setQuantita(pacchetto.getQuantita());
        dto.setPrezzo(pacchetto.getPrezzo());
        dto.setIndirizzo(pacchetto.getIndirizzo());
        dto.setCreatoDa(pacchetto.getCreatoDa());
        dto.setStato(pacchetto.getStato().name());
        dto.setCommento(pacchetto.getCommento());

        if (pacchetto.getCertificati() != null) {
            dto.setCertificati(List.of(pacchetto.getCertificati().split(",")));
        }
        if (pacchetto.getFoto() != null) {
            dto.setFoto(List.of(pacchetto.getFoto().split(",")));
        }

        dto.setProdottiNomi(
                pacchetto.getProdotti().stream()
                        .map(ProdottoEntity::getNome) // solo i nomi
                        .toList()
        );

        model.addAttribute("elemento", dto);
        model.addAttribute("tipo", "pacchetto");
        return "marketplace/dettagli";
    }


    @GetMapping("/trasformato/{id}")
    public String mostraDettagliTrasformato(@PathVariable Long id, Model model) {
        ProdottoTrasformatoEntity trasformato = prodottoTrasformatoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto trasformato non trovato"));
        model.addAttribute("elemento", trasformato);
        model.addAttribute("tipo", "trasformato");
        return "marketplace/dettagli";
    }

    @GetMapping("/visita/{id}")
    public String mostraDettagliVisita(@PathVariable Long id, Model model) {
        VisitaInvitoEntity visita = visitaInvitoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Visita non trovata"));
        model.addAttribute("elemento", visita);
        model.addAttribute("tipo", "visita");
        return "marketplace/dettagli";
    }

    @GetMapping("/fiera/{id}")
    public String mostraDettagliFiera(@PathVariable Long id, Model model) {
        FieraEntity fiera = fieraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fiera non trovata"));
        model.addAttribute("elemento", fiera);
        model.addAttribute("tipo", "fiera");
        return "marketplace/dettagli";
    }
}
