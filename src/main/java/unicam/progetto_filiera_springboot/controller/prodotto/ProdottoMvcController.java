package unicam.progetto_filiera_springboot.controller.prodotto;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import unicam.progetto_filiera_springboot.application.dto.ProdottoForm;
import unicam.progetto_filiera_springboot.application.service.ProdottoService;
import unicam.progetto_filiera_springboot.controller.error.UploadException;
import unicam.progetto_filiera_springboot.strategy.validation.ValidationException;
import unicam.progetto_filiera_springboot.repository.UtenteRepository;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/produttore/prodotti")
public class ProdottoMvcController {

    private final ProdottoService prodottoService;
    private final UtenteRepository utenteRepository;

    public ProdottoMvcController(ProdottoService prodottoService,
                                 UtenteRepository utenteRepository) {
        this.prodottoService = prodottoService;
        this.utenteRepository = utenteRepository;
    }

    @GetMapping("/nuovo")
    public String nuovo(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();

        // Ruolo per header pagina (letto da DB)
        var utente = utenteRepository.findById(username).orElse(null);
        if (utente == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", username);
        model.addAttribute("ruolo", utente.getRuolo());

        // Riepilogo prodotti del produttore
        var prodotti = prodottoService.prodottiDi(username);
        model.addAttribute("prodotti", prodotti);

        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new ProdottoForm());
        }
        return "produttore/index";
    }

    @PostMapping(value = "/invia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String invia(@Valid @ModelAttribute("form") ProdottoForm form,
                        BindingResult binding,
                        @RequestParam("foto") List<MultipartFile> foto,
                        @RequestParam("certificati") List<MultipartFile> certificati,
                        Principal principal,
                        RedirectAttributes ra) {

        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();

        // 1) Errori di validazione dei campi del form
        if (binding.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.form", binding);
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("errorMsg", "Correggi i campi evidenziati. Foto e certificati sono obbligatori.");
            return "redirect:/produttore/prodotti/nuovo";
        }

        // 2) Verifiche server-side su file obbligatori (almeno uno per ciascuna categoria)
        boolean noFoto = (foto == null) || foto.stream().allMatch(MultipartFile::isEmpty);
        boolean noCert = (certificati == null) || certificati.stream().allMatch(MultipartFile::isEmpty);
        if (noFoto || noCert) {
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("errorMsg", "Devi caricare almeno una foto e almeno un certificato.");
            return "redirect:/produttore/prodotti/nuovo";
        }

        // 3) Chiamata service + gestione elegante errori
        try {
            var resp = prodottoService.creaProdottoConFile(form, username, foto, certificati);
            ra.addFlashAttribute("successMsg", "Prodotto inviato al Curatore. ID: " + resp.getId());
            return "redirect:/produttore/prodotti/nuovo";

        } catch (UploadException | ValidationException e) {
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/produttore/prodotti/nuovo";

        } catch (Exception e) {
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("errorMsg", "Errore imprevisto durante l’invio del prodotto.");
            return "redirect:/produttore/prodotti/nuovo";
        }
    }
}
