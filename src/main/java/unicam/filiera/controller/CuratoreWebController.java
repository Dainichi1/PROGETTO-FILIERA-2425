package unicam.filiera.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.dto.PacchettoViewDto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.ProdottoTrasformato;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.service.ProdottoService;
import unicam.filiera.service.PacchettoService;
import unicam.filiera.service.ProdottoTrasformatoService;

import java.util.List;

@Controller
@RequestMapping("/curatore")
public class CuratoreWebController {

    private final ProdottoService prodottoService;
    private final PacchettoService pacchettoService;
    private final ProdottoTrasformatoService prodottoTrasformatoService;

    @Autowired
    public CuratoreWebController(ProdottoService prodottoService,
                                 PacchettoService pacchettoService,
                                 ProdottoTrasformatoService prodottoTrasformatoService) {
        this.prodottoService = prodottoService;
        this.pacchettoService = pacchettoService;
        this.prodottoTrasformatoService = prodottoTrasformatoService;
    }

    /**
     * Mostra la dashboard del curatore con prodotti e pacchetti in attesa.
     */
    @GetMapping("/dashboard")
    public String dashboardCuratore(Model model) {
        List<Prodotto> prodottiInAttesa = prodottoService.getProdottiByStato(StatoProdotto.IN_ATTESA);
        List<PacchettoViewDto> pacchettiInAttesa = pacchettoService.getPacchettiViewByStato(StatoProdotto.IN_ATTESA);
        List<ProdottoTrasformato> trasformatiInAttesa = prodottoTrasformatoService.getProdottiTrasformatiByStato(StatoProdotto.IN_ATTESA);

        model.addAttribute("prodotti", prodottiInAttesa);
        model.addAttribute("pacchetti", pacchettiInAttesa);
        model.addAttribute("trasformati", trasformatiInAttesa);

        return "dashboard/curatore";
    }

    /* ===============================
       PRODOTTI
       =============================== */

    @PostMapping("/approvaProdotto")
    public String approvaProdotto(@RequestParam("nome") String nome,
                                  @RequestParam("creatore") String creatore) {
        try {
            prodottoService.cambiaStatoProdotto(nome, creatore, StatoProdotto.APPROVATO, null);
        } catch (Exception e) {
            System.err.println("Errore approvazione prodotto: " + e.getMessage());
        }
        return "redirect:/curatore/dashboard";
    }

    @PostMapping("/rifiutaProdotto")
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

    /* ===============================
       PACCHETTI
       =============================== */

    @PostMapping("/approvaPacchetto")
    public String approvaPacchetto(@RequestParam("nome") String nome,
                                   @RequestParam("creatore") String creatore) {
        try {
            pacchettoService.cambiaStatoPacchetto(nome, creatore, StatoProdotto.APPROVATO, null);
        } catch (Exception e) {
            System.err.println("Errore approvazione pacchetto: " + e.getMessage());
        }
        return "redirect:/curatore/dashboard";
    }

    @PostMapping("/rifiutaPacchetto")
    public String rifiutaPacchetto(@RequestParam("nome") String nome,
                                   @RequestParam("creatore") String creatore,
                                   @RequestParam(value = "commento", required = false) String commento) {
        try {
            pacchettoService.cambiaStatoPacchetto(nome, creatore, StatoProdotto.RIFIUTATO, commento);
        } catch (Exception e) {
            System.err.println("Errore rifiuto pacchetto: " + e.getMessage());
        }
        return "redirect:/curatore/dashboard";
    }

    /* ===============================
   TRASFORMATI
   =============================== */

    @PostMapping("/approvaTrasformato")
    public String approvaTrasformato(@RequestParam("nome") String nome,
                                     @RequestParam("creatore") String creatore) {
        try {
            prodottoTrasformatoService.cambiaStatoProdottoTrasformato(nome, creatore, StatoProdotto.APPROVATO, null);
        } catch (Exception e) {
            System.err.println("Errore approvazione trasformato: " + e.getMessage());
        }
        return "redirect:/curatore/dashboard";
    }

    @PostMapping("/rifiutaTrasformato")
    public String rifiutaTrasformato(@RequestParam("nome") String nome,
                                     @RequestParam("creatore") String creatore,
                                     @RequestParam(value = "commento", required = false) String commento) {
        try {
            prodottoTrasformatoService.cambiaStatoProdottoTrasformato(nome, creatore, StatoProdotto.RIFIUTATO, commento);
        } catch (Exception e) {
            System.err.println("Errore rifiuto trasformato: " + e.getMessage());
        }
        return "redirect:/curatore/dashboard";
    }

}
