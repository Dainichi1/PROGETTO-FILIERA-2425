package unicam.filiera.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.entity.PacchettoEntity;
import unicam.filiera.repository.ProdottoRepository;
import unicam.filiera.repository.PacchettoRepository;
import unicam.filiera.model.StatoProdotto;

import java.util.List;

@Controller
@RequestMapping("/marketplace")
public class MarketplaceWebController {

    private final ProdottoRepository prodottoRepository;
    private final PacchettoRepository pacchettoRepository;

    public MarketplaceWebController(ProdottoRepository prodottoRepository,
                                    PacchettoRepository pacchettoRepository) {
        this.prodottoRepository = prodottoRepository;
        this.pacchettoRepository = pacchettoRepository;
    }

    /**
     * Mostra lista prodotti e pacchetti approvati
     */
    @GetMapping
    public String mostraMarketplace(Model model) {
        List<ProdottoEntity> prodotti = prodottoRepository.findByStato(StatoProdotto.APPROVATO);
        List<PacchettoEntity> pacchetti = pacchettoRepository.findByStato(StatoProdotto.APPROVATO);

        model.addAttribute("prodotti", prodotti);
        model.addAttribute("pacchetti", pacchetti);

        return "marketplace/lista"; // pagina con layout a due colonne
    }

    /**
     * Mostra dettagli di un singolo prodotto
     */
    @GetMapping("/prodotto/{id}")
    public String mostraDettagliProdotto(@PathVariable Long id, Model model) {
        ProdottoEntity prodotto = prodottoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto non trovato"));
        model.addAttribute("elemento", prodotto);
        model.addAttribute("tipo", "prodotto");
        return "marketplace/dettagli";
    }

    /**
     * Mostra dettagli di un singolo pacchetto
     */
    @GetMapping("/pacchetto/{id}")
    public String mostraDettagliPacchetto(@PathVariable Long id, Model model) {
        PacchettoEntity pacchetto = pacchettoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto non trovato"));
        model.addAttribute("elemento", pacchetto);
        model.addAttribute("tipo", "pacchetto");
        return "marketplace/dettagli";
    }
}
