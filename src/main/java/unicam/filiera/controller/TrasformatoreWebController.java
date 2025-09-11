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
import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.service.ProdottoService;
import unicam.filiera.service.ProdottoTrasformatoService;
import unicam.filiera.service.UtenteService;

import java.util.List;

@Controller
@RequestMapping("/trasformatore")
public class TrasformatoreWebController extends AbstractCreationController<ProdottoTrasformatoDto> {

    private final ProdottoTrasformatoService trasformatoService;
    private final ProdottoService prodottoService;
    private final UtenteService utenteService;

    @Autowired
    public TrasformatoreWebController(ProdottoTrasformatoService trasformatoService,
                                      ProdottoService prodottoService,
                                      UtenteService utenteService) {
        this.trasformatoService = trasformatoService;
        this.prodottoService = prodottoService;
        this.utenteService = utenteService;
    }

    @ModelAttribute("trasformatoDto")
    public ProdottoTrasformatoDto trasformatoDto() {
        return newDto();
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
        model.addAttribute("produttori", utenteService.getProduttori());
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

    // ======= ENDPOINTS =======

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        return showDashboard(model, auth);
    }

    @PostMapping("/crea")
    public String crea(@Valid @ModelAttribute("trasformatoDto") ProdottoTrasformatoDto dto,
                       BindingResult br,
                       Authentication auth,
                       RedirectAttributes ra,
                       Model model) {
        return createItem(dto, br, auth, ra, model);
    }

    /**
     * Endpoint JSON per ottenere i prodotti APPROVATI di un produttore specifico.
     * Usato dalla modale "Aggiungi fase" (trasformatore.js).
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
