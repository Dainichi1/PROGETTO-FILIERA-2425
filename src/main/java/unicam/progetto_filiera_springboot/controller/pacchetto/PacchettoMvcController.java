package unicam.progetto_filiera_springboot.controller.pacchetto;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.progetto_filiera_springboot.application.dto.PacchettoForm;
import unicam.progetto_filiera_springboot.application.service.PacchettoService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/distributore/pacchetti")
public class PacchettoMvcController {

    private final PacchettoService pacchettoService;

    public PacchettoMvcController(PacchettoService pacchettoService) {
        this.pacchettoService = pacchettoService;
    }

    @PostMapping("/crea")
    public String createPacchetto(
            @Valid @ModelAttribute("pacchettoForm") PacchettoForm form,
            BindingResult binding,
            @RequestParam("foto") List<MultipartFile> foto,
            @RequestParam("certificati") List<MultipartFile> certificati,
            Principal principal,
            RedirectAttributes ra
    ) {
        // Utente autenticato?
        if (principal == null) {
            ra.addFlashAttribute("errorMsg", "Sessione non valida. Esegui di nuovo l’accesso.");
            return "redirect:/login";
        }
        String username = principal.getName();

        // 1) Validazione form standard
        if (binding.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.pacchettoForm", binding);
            ra.addFlashAttribute("pacchettoForm", form);
            ra.addFlashAttribute("errorMsg", "Correggi i campi evidenziati. Tutti i campi, almeno due prodotti, almeno una foto e almeno un certificato sono obbligatori.");
            return "redirect:/distributore";
        }

        // 2) Validazione file obbligatori lato server
        boolean noFoto = (foto == null) || foto.stream().allMatch(MultipartFile::isEmpty);
        boolean noCert = (certificati == null) || certificati.stream().allMatch(MultipartFile::isEmpty);
        if (noFoto || noCert) {
            ra.addFlashAttribute("pacchettoForm", form);
            ra.addFlashAttribute("errorMsg", "Devi caricare almeno una foto e almeno un certificato.");
            return "redirect:/distributore";
        }

        // 3) Service e gestione errori
        try {
            pacchettoService.creaPacchettoConFile(form, username, foto, certificati);
            ra.addFlashAttribute("successMsg", "Pacchetto inviato al Curatore per approvazione.");
            return "redirect:/distributore";
        } catch (Exception e) {
            ra.addFlashAttribute("pacchettoForm", form);
            ra.addFlashAttribute("errorMsg", e.getMessage() != null ? e.getMessage() : "Errore durante la creazione del pacchetto.");
            return "redirect:/distributore";
        }
    }
}
