package unicam.filiera.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera.controller.base.AbstractCreationController;
import unicam.filiera.dto.ItemTipo;
import unicam.filiera.dto.PrenotazioneVisitaDto;
import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.service.*;

import java.util.List;

@Controller
@RequestMapping("/trasformatore")
public class TrasformatoreWebController extends AbstractCreationController<ProdottoTrasformatoDto> {

    private final ProdottoTrasformatoService trasformatoService;
    private final ProdottoService prodottoService;
    private final UtenteService utenteService;
    private final VisitaInvitoService visitaInvitoService;
    private final PrenotazioneVisitaService prenotazioneVisitaService;

    @Autowired
    public TrasformatoreWebController(ProdottoTrasformatoService trasformatoService,
                                      ProdottoService prodottoService,
                                      UtenteService utenteService,
                                      VisitaInvitoService visitaInvitoService,
                                      PrenotazioneVisitaService prenotazioneVisitaService) {
        this.trasformatoService = trasformatoService;
        this.prodottoService = prodottoService;
        this.utenteService = utenteService;
        this.visitaInvitoService = visitaInvitoService;
        this.prenotazioneVisitaService = prenotazioneVisitaService;
    }

    @Override
    protected ProdottoTrasformatoDto newDto() {
        ProdottoTrasformatoDto d = new ProdottoTrasformatoDto();
        d.setTipo(ItemTipo.TRASFORMATO);
        return d;
    }

    @Override
    protected String getDtoName() {
        return "trasformatoDto";
    }

    @Override
    protected String getViewName() {
        return "dashboard/trasformatore";
    }

    @Override
    protected String getRedirectPath() {
        return "/trasformatore/dashboard";
    }

    @Override
    protected void loadDashboardLists(Model model, String username) {
        model.addAttribute("trasformati", trasformatoService.getProdottiTrasformatiCreatiDa(username));
        model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
        model.addAttribute("visiteDisponibili", visitaInvitoService.getVisiteByRuoloDestinatario("trasformatore"));
        model.addAttribute("produttori", utenteService.getProduttori());
        model.addAttribute("prenotazioni", prenotazioneVisitaService.getPrenotazioniByVenditore(username));
        model.addAttribute("prenotazioneVisitaDto", new PrenotazioneVisitaDto());
    }

    @Override
    protected void doCreate(ProdottoTrasformatoDto dto, String username) throws Exception {
        trasformatoService.creaProdottoTrasformato(dto, username);
    }

    @Override
    protected String getSuccessMessage() {
        return "Prodotto trasformato inviato al Curatore con successo";
    }

    @Override
    protected void doDelete(Long id, String username) throws Exception {
        trasformatoService.eliminaById(id, username);
    }

    /**
     * Endpoint JSON per ottenere i prodotti APPROVATI di un produttore specifico.
     * Usato dalla tabella/gestione fasi (trasformatore.js).
     */
    @GetMapping("/prodotti/{username}")
    @ResponseBody
    public List<ProdottoDto> getProdottiApprovatiByProduttore(@PathVariable String username) {
        return prodottoService.getProdottiApprovatiByProduttore(username)
                .stream()
                .map(p -> {
                    ProdottoDto dto = new ProdottoDto();
                    dto.setId(p.getId());
                    dto.setNome(p.getNome());
                    return dto;
                })
                .toList();
    }
}
