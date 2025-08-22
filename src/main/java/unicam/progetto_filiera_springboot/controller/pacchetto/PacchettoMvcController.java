package unicam.progetto_filiera_springboot.controller.pacchetto;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.progetto_filiera_springboot.application.dto.PacchettoForm;
import unicam.progetto_filiera_springboot.application.service.PacchettoService;
import unicam.progetto_filiera_springboot.controller.pacchetto.validation.PacchettoValidator;
import unicam.progetto_filiera_springboot.controller.error.UploadException;
import unicam.progetto_filiera_springboot.strategy.validation.ValidationException;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/distributore/pacchetti")
public class PacchettoMvcController {

    private final PacchettoService pacchettoService;
    private final PacchettoValidator pacchettoValidator;

    public PacchettoMvcController(PacchettoService pacchettoService,
                                  PacchettoValidator pacchettoValidator) {
        this.pacchettoService = pacchettoService;
        this.pacchettoValidator = pacchettoValidator;
    }

    @InitBinder("pacchettoForm")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(pacchettoValidator);
    }

    @PostMapping(value = "/crea", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createPacchetto(@Valid @ModelAttribute("pacchettoForm") PacchettoForm form,
                                  BindingResult binding,
                                  @RequestParam(value = "foto", required = false) List<MultipartFile> foto,
                                  @RequestParam(value = "certificati", required = false) List<MultipartFile> certificati,
                                  Principal principal,
                                  RedirectAttributes ra) {

        if (principal == null) {
            ra.addFlashAttribute("errorMsg", "Sessione non valida. Esegui di nuovo l’accesso.");
            return "redirect:/login";
        }
        String username = principal.getName();

        // 1) Bean Validation + Validator custom
        if (binding.hasErrors()) {
            return backWithError(ra, form, binding,
                    "Correggi i campi evidenziati. Tutti i campi, almeno due prodotti, almeno una foto e almeno un certificato sono obbligatori.");
        }

        // 2) Verifica obbligatorietà file (almeno un file non vuoto per tipo)
        boolean noFoto = (foto == null) || foto.isEmpty() || foto.stream().allMatch(MultipartFile::isEmpty);
        boolean noCert = (certificati == null) || certificati.isEmpty() || certificati.stream().allMatch(MultipartFile::isEmpty);

        if (noFoto || noCert) {
            // costruiamo un BindingResult ad hoc per segnalare gli errori ai campi "foto" e "certificati"
            BeanPropertyBindingResult filesBinding = new BeanPropertyBindingResult(form, "pacchettoForm");
            if (noFoto) {
                filesBinding.addError(new FieldError("pacchettoForm", "foto", "Carica almeno una foto"));
            }
            if (noCert) {
                filesBinding.addError(new FieldError("pacchettoForm", "certificati", "Carica almeno un certificato"));
            }
            return backWithError(ra, form, filesBinding,
                    "Correggi i campi evidenziati. Foto e certificati sono obbligatori.");
        }

        // 3) Chiamata al service
        try {
            pacchettoService.creaPacchettoConFile(form, username, foto, certificati);
            ra.addFlashAttribute("successMsg", "Pacchetto inviato al Curatore per approvazione.");
            return "redirect:/distributore";

        } catch (UploadException | ValidationException e) {
            // Se il service ha una motivazione specifica, la mostriamo come messaggio di errore generale.
            return backWithError(ra, form, null,
                    (e.getMessage() != null && !e.getMessage().isBlank())
                            ? e.getMessage()
                            : "Errore di validazione durante la creazione del pacchetto.");

        } catch (Exception e) {
            return backWithError(ra, form, null, "Errore durante la creazione del pacchetto.");
        }
    }

    /**
     * Utility: centralizza il ritorno al form con i messaggi e l'apertura del form.
     */
    private String backWithError(RedirectAttributes ra, PacchettoForm form, BindingResult binding, String msg) {
        if (binding != null) {
            // fondamentale: il nome dopo "BindingResult." deve coincidere con l'attributo del th:object
            ra.addFlashAttribute("org.springframework.validation.BindingResult.pacchettoForm", binding);
        }
        ra.addFlashAttribute("pacchettoForm", form);
        ra.addFlashAttribute("errorMsg", msg);
        ra.addFlashAttribute("hasFormErrors", true); // riapre il form nella view
        return "redirect:/distributore";
    }
}
