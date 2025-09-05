package unicam.filiera.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.entity.PacchettoEntity;
import unicam.filiera.entity.ProdottoTrasformatoEntity;
import unicam.filiera.repository.ProdottoRepository;
import unicam.filiera.repository.PacchettoRepository;
import unicam.filiera.repository.ProdottoTrasformatoRepository;
import unicam.filiera.model.StatoProdotto;

import java.util.List;

@Controller
@RequestMapping("/marketplace")
public class MarketplaceWebController {

    private final ProdottoRepository prodottoRepository;
    private final PacchettoRepository pacchettoRepository;
    private final ProdottoTrasformatoRepository prodottoTrasformatoRepository;

    public MarketplaceWebController(ProdottoRepository prodottoRepository,
                                    PacchettoRepository pacchettoRepository,
                                    ProdottoTrasformatoRepository prodottoTrasformatoRepository) {
        this.prodottoRepository = prodottoRepository;
        this.pacchettoRepository = pacchettoRepository;
        this.prodottoTrasformatoRepository = prodottoTrasformatoRepository;
    }

    @GetMapping
    public String mostraMarketplace(Model model) {
        List<ProdottoEntity> prodotti = prodottoRepository.findByStato(StatoProdotto.APPROVATO);
        List<PacchettoEntity> pacchetti = pacchettoRepository.findByStato(StatoProdotto.APPROVATO);
        List<ProdottoTrasformatoEntity> trasformati = prodottoTrasformatoRepository.findByStato(StatoProdotto.APPROVATO);

        model.addAttribute("prodotti", prodotti);
        model.addAttribute("pacchetti", pacchetti);
        model.addAttribute("trasformati", trasformati);

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
        model.addAttribute("elemento", pacchetto);
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
}
