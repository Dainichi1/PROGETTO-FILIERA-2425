package unicam.filiera_agricola_2425.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera_agricola_2425.dtos.ProdottoForm;
import unicam.filiera_agricola_2425.models.Produttore;
import unicam.filiera_agricola_2425.models.UtenteAutenticato;
import unicam.filiera_agricola_2425.repositories.ProdottoRepository;
import unicam.filiera_agricola_2425.repositories.UtenteRepository;

import java.util.Optional;

@Controller
@RequestMapping("/produttore")
public class ProdottoController {

    @Autowired
    private ProdottoRepository prodottoRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    @GetMapping("/dashboard")
    public String dashboardProduttore(Model model,
                                      HttpSession session,
                                      @RequestParam(name = "toggleForm", required = false) Boolean toggleForm,
                                      @RequestParam(name = "success", required = false) Boolean success) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Optional<UtenteAutenticato> utenteOpt = utenteRepository.findByUsername(username);
        if (utenteOpt.isEmpty() || !(utenteOpt.get() instanceof Produttore produttore)) {
            return "redirect:/login";
        }

        model.addAttribute("nome", produttore.getNome());
        model.addAttribute("ruolo", produttore.getRuolo());
        model.addAttribute("prodotto", new ProdottoForm());

        // ✅ Mostra/nascondi form
        if (toggleForm != null && toggleForm) {
            model.addAttribute("mostraForm", true);
        }

        if (Boolean.TRUE.equals(success)) {
            model.addAttribute("successo", true);
        }

        model.addAttribute("prodotti", prodottoRepository.findByProduttore(produttore));

        return "produttore_dashboard";
    }



    @PostMapping("/crea-prodotto")
    public String salvaProdotto(@ModelAttribute ProdottoForm prodottoForm, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Optional<UtenteAutenticato> utenteOpt = utenteRepository.findByUsername(username);
        if (utenteOpt.isEmpty() || !(utenteOpt.get() instanceof Produttore produttore)) {
            return "redirect:/login";
        }

        prodottoRepository.save(prodottoForm.toProdotto(produttore));

        // 🔁 Redirect alla dashboard con messaggio di successo
        return "redirect:/produttore/dashboard?success=true";
    }
}
