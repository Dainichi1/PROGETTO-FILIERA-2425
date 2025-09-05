package unicam.filiera.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.service.ProdottoService;
import unicam.filiera.service.ProdottoTrasformatoService;
import unicam.filiera.service.UtenteService;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/trasformatore")
public class TrasformatoreWebController {

    private static final Logger log = LoggerFactory.getLogger(TrasformatoreWebController.class);

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
        return new ProdottoTrasformatoDto();
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Accetta sia "4.0" che "4,0"
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.ITALY);
        binder.registerCustomEditor(Double.class, new CustomNumberEditor(Double.class, numberFormat, true));
        binder.registerCustomEditor(Integer.class, new CustomNumberEditor(Integer.class, numberFormat, true));
    }

    @GetMapping("/dashboard")
    public String dashboardTrasformatore(Model model) {
        prepareCommonModel(model);
        model.addAttribute("showForm", false);
        return "dashboard/trasformatore";
    }

    @GetMapping("/prodotti/{usernameProduttore}")
    @ResponseBody
    public List<Map<String, String>> getProdottiApprovatiByProduttore(@PathVariable String usernameProduttore) {
        return prodottoService.getProdottiApprovatiByProduttore(usernameProduttore)
                .stream()
                .map(p -> Map.of(
                        "id", String.valueOf(p.getId()),
                        "nome", p.getNome()
                ))
                .toList();
    }

    @PostMapping("/crea")
    public String creaProdottoTrasformato(
            @Valid @ModelAttribute("trasformatoDto") ProdottoTrasformatoDto trasformatoDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttrs,
            Model model
    ) {
        // Validazione manuale file
        if (trasformatoDto.getCertificati() == null || trasformatoDto.getCertificati().isEmpty()
                || trasformatoDto.getCertificati().stream().allMatch(MultipartFile::isEmpty)) {
            bindingResult.rejectValue("certificati", "error.certificati", "⚠ Devi caricare almeno un certificato");
        }
        if (trasformatoDto.getFoto() == null || trasformatoDto.getFoto().isEmpty()
                || trasformatoDto.getFoto().stream().allMatch(MultipartFile::isEmpty)) {
            bindingResult.rejectValue("foto", "error.foto", "⚠ Devi caricare almeno una foto");
        }

        // Validazione extra sulle fasi
        if (trasformatoDto.getFasiProduzione() == null || trasformatoDto.getFasiProduzione().size() < 2) {
            bindingResult.rejectValue("fasiProduzione", "error.fasiProduzione",
                    "⚠ Devi inserire almeno 2 fasi di produzione");
        }

        if (bindingResult.hasErrors()) {
            log.warn("❌ Creazione prodotto trasformato fallita per errori di validazione");
            bindingResult.getAllErrors().forEach(err -> log.warn("Errore validazione: {}", err));

            prepareCommonModel(model);
            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);
            return "dashboard/trasformatore";
        }

        try {
            String username = (authentication != null) ? authentication.getName() : "trasformatore_demo";
            trasformatoService.creaProdottoTrasformato(trasformatoDto, username);

            redirectAttrs.addFlashAttribute("successMessage", "✅ Prodotto trasformato inviato al Curatore con successo!");
            return "redirect:/trasformatore/dashboard";

        } catch (Exception ex) {
            log.error("⚠️ Errore nella creazione del prodotto trasformato", ex);
            prepareCommonModel(model);
            model.addAttribute("errorMessage", "Errore: " + ex.getMessage());
            model.addAttribute("showForm", true);
            model.addAttribute("validationFailed", true);
            return "dashboard/trasformatore";
        }
    }

    private void prepareCommonModel(Model model) {
        model.addAttribute("prodottiApprovati", prodottoService.getProdottiByStato(StatoProdotto.APPROVATO));
        model.addAttribute("produttori", utenteService.getProduttori());
    }
}
