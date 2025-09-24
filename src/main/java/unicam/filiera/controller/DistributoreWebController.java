package unicam.filiera.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import unicam.filiera.controller.base.AbstractCreationController;
import unicam.filiera.dto.ItemTipo;
import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.dto.PrenotazioneVisitaDto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.repository.UtenteRepository;
import unicam.filiera.service.EliminazioneProfiloService;
import unicam.filiera.service.PacchettoService;
import unicam.filiera.service.PrenotazioneVisitaService;
import unicam.filiera.service.ProdottoService;
import unicam.filiera.service.VisitaInvitoService;

@Controller
@RequestMapping("/distributore")
public class DistributoreWebController extends AbstractCreationController<PacchettoDto> {

    private final PacchettoService pacchettoService;
    private final ProdottoService prodottoService;
    private final VisitaInvitoService visitaInvitoService;
    private final PrenotazioneVisitaService prenotazioneVisitaService;

    @Autowired
    public DistributoreWebController(PacchettoService pacchettoService,
                                     ProdottoService prodottoService,
                                     VisitaInvitoService visitaInvitoService,
                                     PrenotazioneVisitaService prenotazioneVisitaService,
                                     UtenteRepository utenteRepo,
                                     EliminazioneProfiloService eliminazioneProfiloService) {
        super(utenteRepo, eliminazioneProfiloService); // gestisce utente + eliminazione profilo
        this.pacchettoService = pacchettoService;
        this.prodottoService = prodottoService;
        this.visitaInvitoService = visitaInvitoService;
        this.prenotazioneVisitaService = prenotazioneVisitaService;
    }

    @Override
    protected PacchettoDto newDto() {
        PacchettoDto d = new PacchettoDto();
        d.setTipo(ItemTipo.PACCHETTO);
        return d;
    }

    @Override
    protected String getDtoName() {
        return "pacchettoDto";
    }

    @Override
    protected String getViewName() {
        return "dashboard/distributore";
    }

    @Override
    protected String getRedirectPath() {
        return "/distributore/dashboard";
    }

    @Override
    protected void loadDashboardLists(Model model, String username) {
        model.addAttribute("pacchetti", pacchettoService.getPacchettiViewByCreatore(username));
        model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
        model.addAttribute("visiteDisponibili", visitaInvitoService.getVisiteByRuoloDestinatario("distributore"));
        model.addAttribute("prenotazioni", prenotazioneVisitaService.getPrenotazioniByVenditore(username));
        model.addAttribute("prenotazioneVisitaDto", new PrenotazioneVisitaDto());
    }

    @Override
    protected void doCreate(PacchettoDto dto, String username) throws Exception {
        pacchettoService.creaPacchetto(dto, username);
    }

    @Override
    protected String getSuccessMessage() {
        return "Pacchetto inviato al Curatore con successo";
    }

    @Override
    protected void doDelete(Long id, String username) throws Exception {
        pacchettoService.eliminaById(id, username);
    }
}
