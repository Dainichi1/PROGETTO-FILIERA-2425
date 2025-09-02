package unicam.filiera.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.repository.ProdottoRepository;

import java.util.List;

@Controller
@RequestMapping("/marketplace")
public class MarketplaceWebController {

    private final ProdottoRepository prodottoRepository;

    public MarketplaceWebController(ProdottoRepository prodottoRepository) {
        this.prodottoRepository = prodottoRepository;
    }

    /**
     * Mostra lista prodotti approvati (colonna sinistra)
     */
    @GetMapping
    public String mostraMarketplace(Model model) {
        List<ProdottoEntity> prodotti = prodottoRepository.findByStato(StatoProdotto.APPROVATO);
        model.addAttribute("prodotti", prodotti);
        return "marketplace/lista"; // pagina con layout a due colonne
    }

    /**
     * Mostra dettagli di un singolo prodotto (colonna destra)
     */
    @GetMapping("/{id}")
    public String mostraDettagliProdotto(@PathVariable Long id, Model model) {
        ProdottoEntity prodotto = prodottoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto non trovato"));

        model.addAttribute("prodotto", prodotto);
        return "marketplace/dettagli"; // frammento Thymeleaf per i dettagli
    }
}
