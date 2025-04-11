package unicam.filiera_agricola_2425.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import unicam.filiera_agricola_2425.config.SessionHelper;
import unicam.filiera_agricola_2425.models.Prodotto;
import unicam.filiera_agricola_2425.models.Ruolo;
import unicam.filiera_agricola_2425.models.UtenteAutenticato;
import unicam.filiera_agricola_2425.repositories.ProdottoRepository;
import unicam.filiera_agricola_2425.repositories.UtenteRepository;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/curatore")
public class CuratoreController {

    @Autowired private UtenteRepository utenteRepository;
    @Autowired private ProdottoRepository prodottoRepository;

    @GetMapping("/dashboard")
    public String dashboardCuratore(HttpSession session,
                                    Model model,
                                    @RequestParam(name = "mostraProdotti", required = false) Boolean mostraProdotti) {

        Optional<UtenteAutenticato> utenteOpt = SessionHelper.getUtenteAutenticato(session, utenteRepository, Ruolo.CURATORE);
        if (utenteOpt.isEmpty()) return "redirect:/login";

        UtenteAutenticato curatore = utenteOpt.get();
        model.addAttribute("messaggio", curatore.messaggioDashboard());

        if (Boolean.TRUE.equals(mostraProdotti)) {
            List<Prodotto> prodottiInAttesa = prodottoRepository.findByStato(Prodotto.StatoProdotto.IN_ATTESA_APPROVAZIONE);
            model.addAttribute("prodotti", prodottiInAttesa);
        }

        return "curatore_dashboard";
    }


    @PostMapping("/approva/{id}")
    public String approvaProdotto(@PathVariable Long id, HttpSession session) {
        Optional<UtenteAutenticato> utenteOpt = SessionHelper.getUtenteAutenticato(session, utenteRepository, Ruolo.CURATORE);
        if (utenteOpt.isEmpty()) return "redirect:/login";

        prodottoRepository.findById(id).ifPresent(prodotto -> {
            prodotto.setStato(Prodotto.StatoProdotto.APPROVATO);
            prodotto.setCommentoRifiuto(null);
            prodottoRepository.save(prodotto);
        });

        return "redirect:/curatore/dashboard";
    }

    @PostMapping("/rifiuta/{id}")
    public String rifiutaProdotto(@PathVariable Long id, @RequestParam String commento, HttpSession session) {
        Optional<UtenteAutenticato> utenteOpt = SessionHelper.getUtenteAutenticato(session, utenteRepository, Ruolo.CURATORE);
        if (utenteOpt.isEmpty()) return "redirect:/login";

        prodottoRepository.findById(id).ifPresent(prodotto -> {
            prodotto.setStato(Prodotto.StatoProdotto.RIFIUTATO);
            prodotto.setCommentoRifiuto(commento);
            prodottoRepository.save(prodotto);
        });

        return "redirect:/curatore/dashboard";
    }
}
