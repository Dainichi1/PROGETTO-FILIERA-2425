package unicam.filiera.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.service.ProdottoService;

import java.util.List;

@Controller
@RequestMapping("/curatore")
public class CuratoreWebController {

    private final ProdottoService prodottoService;

    @Autowired
    public CuratoreWebController(ProdottoService prodottoService) {
        this.prodottoService = prodottoService;
    }

    /**
     * Mostra la dashboard del curatore con tutti i prodotti in attesa di approvazione.
     */
    @GetMapping("/dashboard")
    public String dashboardCuratore(Model model) {
        List<Prodotto> prodottiInAttesa = prodottoService.getProdottiByStato(StatoProdotto.IN_ATTESA);
        model.addAttribute("prodotti", prodottiInAttesa);
        return "dashboard/curatore";
    }

    /**
     * Azione di approvazione prodotto.
     */
    @PostMapping("/approva")
    public String approvaProdotto(@RequestParam("nome") String nome,
                                  @RequestParam("creatore") String creatore) {
        try {
            prodottoService.cambiaStatoProdotto(nome, creatore, StatoProdotto.APPROVATO, null);
        } catch (Exception e) {
            System.err.println("Errore approvazione prodotto: " + e.getMessage());
        }
        return "redirect:/curatore/dashboard";
    }

    /**
     * Azione di rifiuto prodotto con commento.
     */
    @PostMapping("/rifiuta")
    public String rifiutaProdotto(@RequestParam("nome") String nome,
                                  @RequestParam("creatore") String creatore,
                                  @RequestParam(value = "commento", required = false) String commento) {
        try {
            prodottoService.cambiaStatoProdotto(nome, creatore, StatoProdotto.RIFIUTATO, commento);
        } catch (Exception e) {
            System.err.println("Errore rifiuto prodotto: " + e.getMessage());
        }
        return "redirect:/curatore/dashboard";
    }

}
