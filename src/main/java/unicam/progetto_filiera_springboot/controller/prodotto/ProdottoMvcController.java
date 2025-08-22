package unicam.progetto_filiera_springboot.controller.prodotto;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.progetto_filiera_springboot.application.dto.ProdottoForm;
import unicam.progetto_filiera_springboot.application.service.ProdottoService;
import unicam.progetto_filiera_springboot.controller.error.UploadException;
import unicam.progetto_filiera_springboot.controller.prodotto.validation.ProdottoValidator;
import unicam.progetto_filiera_springboot.strategy.validation.ValidationException;
import unicam.progetto_filiera_springboot.repository.UtenteRepository;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/produttore/prodotti")
public class ProdottoMvcController {

    private final ProdottoService prodottoService;
    private final UtenteRepository utenteRepository;
    private final ProdottoValidator prodottoValidator;

    public ProdottoMvcController(ProdottoService prodottoService,
                                 UtenteRepository utenteRepository,
                                 ProdottoValidator prodottoValidator) {
        this.prodottoService = prodottoService;
        this.utenteRepository = utenteRepository;
        this.prodottoValidator = prodottoValidator;
    }

    @InitBinder("form")
    void initBinder(WebDataBinder binder) {
        binder.addValidators(prodottoValidator);
    }

    @GetMapping("/nuovo")
    public String nuovo(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        String username = principal.getName();

        var utente = utenteRepository.findById(username).orElse(null);
        if (utente == null) return "redirect:/login";

        model.addAttribute("username", username);
        model.addAttribute("ruolo", utente.getRuolo());
        model.addAttribute("prodotti", prodottoService.prodottiDi(username));

        if (!model.containsAttribute("form")) model.addAttribute("form", new ProdottoForm());
        if (!model.containsAttribute("hasFormErrors")) model.addAttribute("hasFormErrors", false);

        return "produttore/index";
    }

    @PostMapping(value = "/invia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String invia(@Valid @ModelAttribute("form") ProdottoForm form,
                        BindingResult binding,
                        @RequestParam(value = "foto", required = false) List<MultipartFile> foto,
                        @RequestParam(value = "certificati", required = false) List<MultipartFile> certificati,
                        Principal principal,
                        RedirectAttributes ra) {

        if (principal == null) return "redirect:/login";
        String username = principal.getName();

        // 1) Errori DTO
        if (binding.hasErrors()) {
            return backWithError(ra, form, binding,
                    "Correggi i campi evidenziati. Foto e certificati sono obbligatori.");
        }

        // 2) Obbligatorietà file (almeno uno non vuoto per tipo)
        boolean noFoto = (foto == null) || foto.isEmpty() || foto.stream().allMatch(MultipartFile::isEmpty);
        boolean noCert = (certificati == null) || certificati.isEmpty() || certificati.stream().allMatch(MultipartFile::isEmpty);
        if (noFoto || noCert) {
            BeanPropertyBindingResult filesBr = new BeanPropertyBindingResult(form, "form");
            if (noFoto) filesBr.addError(new FieldError("form", "foto", "Carica almeno una foto"));
            if (noCert) filesBr.addError(new FieldError("form", "certificati", "Carica almeno un certificato"));
            return backWithError(ra, form, filesBr, "Foto e certificati sono obbligatori.");
        }

        // 3) Service
        try {
            prodottoService.creaProdottoConFile(form, username, foto, certificati);
            ra.addFlashAttribute("successMsg", "Prodotto inviato al Curatore per approvazione");
            return "redirect:/produttore/prodotti/nuovo";
        } catch (UploadException | ValidationException e) {
            return backWithError(ra, form, null,
                    (e.getMessage() != null && !e.getMessage().isBlank())
                            ? e.getMessage()
                            : "Errore di validazione durante l’invio del prodotto.");
        } catch (Exception e) {
            return backWithError(ra, form, null, "Errore imprevisto durante l’invio del prodotto.");
        }
    }

    private String backWithError(RedirectAttributes ra, ProdottoForm form, BindingResult binding, String msg) {
        if (binding != null) {
            // deve corrispondere al th:object="${form}"
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", binding);
        }
        ra.addFlashAttribute("form", form);
        ra.addFlashAttribute("errorMsg", msg);
        ra.addFlashAttribute("hasFormErrors", true);
        return "redirect:/produttore/prodotti/nuovo";
    }
}
