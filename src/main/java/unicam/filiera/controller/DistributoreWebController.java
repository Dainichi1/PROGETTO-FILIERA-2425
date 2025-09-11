package unicam.filiera.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.filiera.controller.base.AbstractCreationController;
import unicam.filiera.dto.ItemTipo;
import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.service.PacchettoService;
import unicam.filiera.service.ProdottoService;

@Controller
@RequestMapping("/distributore")
public class DistributoreWebController extends AbstractCreationController<PacchettoDto> {

    private final PacchettoService pacchettoService;
    private final ProdottoService prodottoService;

    @Autowired
    public DistributoreWebController(PacchettoService pacchettoService,
                                     ProdottoService prodottoService) {
        this.pacchettoService = pacchettoService;
        this.prodottoService = prodottoService;
    }

    @ModelAttribute("pacchettoDto")
    public PacchettoDto pacchettoDto() {
        return newDto();
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

    // ======= ENDPOINTS =======

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        return showDashboard(model, auth);
    }

    @PostMapping("/crea")
    public String crea(@Valid @ModelAttribute("pacchettoDto") PacchettoDto dto,
                       BindingResult br,
                       Authentication auth,
                       RedirectAttributes ra,
                       Model model) {
        return createItem(dto, br, auth, ra, model);
    }
}
