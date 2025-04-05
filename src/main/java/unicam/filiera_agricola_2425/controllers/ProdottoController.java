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
    public String dashboardProduttore(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }

        Optional<UtenteAutenticato> utenteOpt = utenteRepository.findByUsername(username);
        if (utenteOpt.isEmpty() || !(utenteOpt.get() instanceof Produttore produttore)) {
            return "redirect:/login";
        }

        model.addAttribute("nome", produttore.getNome());
        model.addAttribute("ruolo", produttore.getRuolo());
        return "produttore_dashboard";
    }


    @GetMapping("/crea-prodotto")
    public String mostraForm(Model model) {
        model.addAttribute("prodotto", new ProdottoForm());
        return "crea_prodotto";
    }

    @PostMapping("/crea-prodotto")
    public String salvaProdotto(@ModelAttribute ProdottoForm prodottoForm, HttpSession session) {
        // Recupera username dalla sessione
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }

        // Cerca il produttore nel DB
        Optional<UtenteAutenticato> utenteOpt = utenteRepository.findByUsername(username);
        if (utenteOpt.isEmpty() || !(utenteOpt.get() instanceof Produttore produttore)) {
            return "redirect:/login"; // non è un produttore valido
        }

        // Salva prodotto associato al produttore
        prodottoRepository.save(prodottoForm.toProdotto(produttore));

        return "redirect:/produttore/dashboard";
    }
}
