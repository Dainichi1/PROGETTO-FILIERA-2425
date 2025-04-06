package unicam.filiera_agricola_2425.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera_agricola_2425.dtos.ProdottoMarketplaceDTO;
import unicam.filiera_agricola_2425.models.Prodotto;
import unicam.filiera_agricola_2425.repositories.ProdottoRepository;

import java.util.List;
import java.util.Optional;

@Controller
public class MarketplaceController {

    @Autowired
    private ProdottoRepository prodottoRepository;

    @GetMapping("/marketplace")
    public String mostraProdottiApprovati(Model model) {
        List<Prodotto> prodottiApprovati = prodottoRepository.findByStato(Prodotto.StatoProdotto.APPROVATO);

        List<ProdottoMarketplaceDTO> dtoList = prodottiApprovati.stream()
                .map(ProdottoMarketplaceDTO::new)
                .toList();

        model.addAttribute("prodotti", dtoList);
        return "marketplace";
    }

    @GetMapping("/marketplace/{id}")
    public String dettagliProdotto(@PathVariable Long id, Model model) {
        return prodottoRepository.findById(id)
                .filter(p -> p.getStato() == Prodotto.StatoProdotto.APPROVATO)
                .map(ProdottoMarketplaceDTO::new)
                .map(dto -> {
                    model.addAttribute("prodotto", dto);
                    return "marketplace_dettaglio";
                })
                .orElse("redirect:/marketplace");
    }
}

