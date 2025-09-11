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
import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.service.ProdottoService;

import java.util.List;

@Controller
@RequestMapping("/produttore")
public class ProduttoreWebController extends AbstractCreationController<ProdottoDto> {

    private final ProdottoService prodottoService;

    @Autowired
    public ProduttoreWebController(ProdottoService prodottoService) {
        this.prodottoService = prodottoService;
    }

    @ModelAttribute("prodottoDto")
    public ProdottoDto prodottoDto() {
        return newDto();
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

    // ======= ENDPOINTS =======

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        return showDashboard(model, auth);
    }

    @PostMapping("/crea")
    public String crea(@Valid @ModelAttribute("prodottoDto") ProdottoDto dto,
                       BindingResult br,
                       Authentication auth,
                       RedirectAttributes ra,
                       Model model) {
        return createItem(dto, br, auth, ra, model);
    }
}
