package unicam.filiera.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.dto.VisitaInvitoDto;
import unicam.filiera.dto.FieraDto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.StatoEvento;
import unicam.filiera.service.*;

import java.util.List;

@Controller
@RequestMapping("/marketplace")
public class MarketplaceWebController {

    private final ProdottoService prodottoService;
    private final PacchettoService pacchettoService;
    private final ProdottoTrasformatoService trasformatoService;
    private final VisitaInvitoService visitaInvitoService;
    private final FieraService fieraService;

    public MarketplaceWebController(ProdottoService prodottoService,
                                    PacchettoService pacchettoService,
                                    ProdottoTrasformatoService trasformatoService,
                                    VisitaInvitoService visitaInvitoService,
                                    FieraService fieraService) {
        this.prodottoService = prodottoService;
        this.pacchettoService = pacchettoService;
        this.trasformatoService = trasformatoService;
        this.visitaInvitoService = visitaInvitoService;
        this.fieraService = fieraService;
    }

    @GetMapping
    public String mostraMarketplace(Model model) {
        List<ProdottoDto> prodotti = prodottoService.getProdottiByStato(StatoProdotto.APPROVATO);
        List<PacchettoDto> pacchetti = pacchettoService.getPacchettiByStato(StatoProdotto.APPROVATO);
        List<ProdottoTrasformatoDto> trasformati = trasformatoService.getProdottiTrasformatiByStato(StatoProdotto.APPROVATO);
        List<VisitaInvitoDto> visite = visitaInvitoService.getVisiteByStato(StatoEvento.PUBBLICATA);
        List<FieraDto> fiere = fieraService.getFiereByStato(StatoEvento.PUBBLICATA);

        model.addAttribute("prodotti", prodotti);
        model.addAttribute("pacchetti", pacchetti);
        model.addAttribute("trasformati", trasformati);
        model.addAttribute("visite", visite);
        model.addAttribute("fiere", fiere);

        return "marketplace/lista";
    }

    @GetMapping("/prodotto/{id}")
    public String mostraDettagliProdotto(@PathVariable Long id, Model model) {
        ProdottoDto prodotto = prodottoService.findDtoById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto non trovato"));
        model.addAttribute("elemento", prodotto);
        model.addAttribute("tipo", "prodotto");
        return "marketplace/dettagli";
    }

    @GetMapping("/pacchetto/{id}")
    public String mostraDettagliPacchetto(@PathVariable Long id, Model model) {
        PacchettoDto dto = pacchettoService.findDtoById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pacchetto non trovato"));
        model.addAttribute("elemento", dto);
        model.addAttribute("tipo", "pacchetto");
        return "marketplace/dettagli";
    }

    @GetMapping("/trasformato/{id}")
    public String mostraDettagliTrasformato(@PathVariable Long id, Model model) {
        ProdottoTrasformatoDto trasformato = trasformatoService.findDtoById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prodotto trasformato non trovato"));
        model.addAttribute("elemento", trasformato);
        model.addAttribute("tipo", "trasformato");
        return "marketplace/dettagli";
    }

    @GetMapping("/visita/{id}")
    public String mostraDettagliVisita(@PathVariable Long id, Model model) {
        VisitaInvitoDto visita = visitaInvitoService.findDtoById(id)
                .orElseThrow(() -> new IllegalArgumentException("Visita non trovata"));
        model.addAttribute("elemento", visita);
        model.addAttribute("tipo", "visita");
        return "marketplace/dettagli";
    }

    @GetMapping("/fiera/{id}")
    public String mostraDettagliFiera(@PathVariable Long id, Model model) {
        FieraDto fiera = fieraService.findDtoById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fiera non trovata"));
        model.addAttribute("elemento", fiera);
        model.addAttribute("tipo", "fiera");
        return "marketplace/dettagli";
    }
}
