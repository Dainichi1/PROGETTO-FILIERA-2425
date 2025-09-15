package unicam.filiera.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import unicam.filiera.controller.base.AbstractCreationController;
import unicam.filiera.dto.ItemTipo;
import unicam.filiera.dto.PrenotazioneVisitaDto;
import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.service.PrenotazioneVisitaService;
import unicam.filiera.service.ProdottoService;
import unicam.filiera.service.VisitaInvitoService;

import java.util.List;

@Controller
@RequestMapping("/produttore")
public class ProduttoreWebController extends AbstractCreationController<ProdottoDto> {

    private final ProdottoService prodottoService;
    private final VisitaInvitoService visitaInvitoService;
    private final PrenotazioneVisitaService prenotazioneVisitaService;

    @Autowired
    public ProduttoreWebController(ProdottoService prodottoService,
                                   VisitaInvitoService visitaInvitoService,
                                   PrenotazioneVisitaService prenotazioneVisitaService) {
        this.prodottoService = prodottoService;
        this.visitaInvitoService = visitaInvitoService;
        this.prenotazioneVisitaService = prenotazioneVisitaService;
    }

    @Override
    protected ProdottoDto newDto() {
        ProdottoDto d = new ProdottoDto();
        d.setTipo(ItemTipo.PRODOTTO);
        return d;
    }

    @Override
    protected String getDtoName() {
        return "prodottoDto";
    }

    @Override
    protected String getViewName() {
        return "dashboard/produttore";
    }

    @Override
    protected String getRedirectPath() {
        return "/produttore/dashboard";
    }

    @Override
    protected void loadDashboardLists(Model model, String username) {
        List<Prodotto> prodotti = prodottoService.getProdottiCreatiDa(username);
        model.addAttribute("prodotti", prodotti);
        model.addAttribute("visiteDisponibili",
                visitaInvitoService.getVisiteByRuoloDestinatario("produttore"));
        model.addAttribute("prenotazioni",
                prenotazioneVisitaService.getPrenotazioniByVenditore(username));
        model.addAttribute("prenotazioneVisitaDto", new PrenotazioneVisitaDto());
    }

    @Override
    protected void doCreate(ProdottoDto dto, String username) throws Exception {
        prodottoService.creaProdotto(dto, username);
    }

    @Override
    protected String getSuccessMessage() {
        return "Prodotto inviato al Curatore con successo";
    }

    @Override
    protected void doDelete(Long id, String username) throws Exception {
        prodottoService.eliminaById(id, username);
    }
}
