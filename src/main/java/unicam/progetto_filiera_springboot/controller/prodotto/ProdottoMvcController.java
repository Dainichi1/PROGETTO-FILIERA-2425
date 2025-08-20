package unicam.progetto_filiera_springboot.controller.prodotto;

import jakarta.servlet.http.HttpSession;
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
import unicam.progetto_filiera_springboot.domain.actor.Produttore;
import unicam.progetto_filiera_springboot.strategy.validation.ValidationException;
import unicam.progetto_filiera_springboot.controller.error.UploadException;

import java.util.List;

@Controller
@RequestMapping("/produttore/prodotti")
public class ProdottoMvcController {

    private final ProdottoService prodottoService;

    public ProdottoMvcController(ProdottoService prodottoService) {
        this.prodottoService = prodottoService;
    }


    @GetMapping("/nuovo")
    public String nuovo(HttpSession session, Model model) {
        Object attore = session.getAttribute("attore");
        if (!(attore instanceof Produttore p)) {
            return "redirect:/login";
        }

        model.addAttribute("username", p.getUsername());
        model.addAttribute("ruolo", p.getRuolo());

        // *** RIEPILOGO PRODOTTI DEL PRODUTTORE ***
        var prodotti = prodottoService.prodottiDi(p.getUsername());
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
                        HttpSession session,
                        RedirectAttributes ra) {

        Object attore = session.getAttribute("attore");
        if (!(attore instanceof Produttore prod)) {
            return "redirect:/login";
        }

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
            var resp = prodottoService.creaProdottoConFile(form, prod.getUsername(), foto, certificati);
            ra.addFlashAttribute("successMsg", "Prodotto inviato al Curatore. ID: " + resp.getId());
            return "redirect:/produttore/prodotti/nuovo";

        } catch (UploadException | ValidationException e) {
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/produttore/prodotti/nuovo";

        } catch (Exception e) {
            // fallback: evita Whitelabel per qualsiasi imprevisto
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("errorMsg", "Errore imprevisto durante l’invio del prodotto.");
            return "redirect:/produttore/prodotti/nuovo";
        }
    }
}
