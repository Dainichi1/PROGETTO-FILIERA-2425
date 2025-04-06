package unicam.filiera_agricola_2425.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import unicam.filiera_agricola_2425.dtos.ProdottoForm;
import unicam.filiera_agricola_2425.models.*;
import unicam.filiera_agricola_2425.repositories.CertificatoProdottoRepository;
import unicam.filiera_agricola_2425.repositories.ImmagineProdottoRepository;
import unicam.filiera_agricola_2425.repositories.ProdottoRepository;
import unicam.filiera_agricola_2425.repositories.UtenteRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
@RequestMapping("/produttore")
public class ProdottoController {

    @Autowired
    private ProdottoRepository prodottoRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private ImmagineProdottoRepository immagineRepo;

    @Autowired
    private CertificatoProdottoRepository certificatoRepo;

    // Metodo helper centralizzato per sicurezza e leggibilità
    private Optional<Produttore> getProduttoreFromSession(HttpSession session) {
        String username = (String) session.getAttribute("username");
        Object ruoloObj = session.getAttribute("ruolo");

        if (username == null || !(ruoloObj instanceof Ruolo ruolo) || ruolo != Ruolo.PRODUTTORE)
            return Optional.empty();

        return utenteRepository.findByUsername(username)
                .filter(Produttore.class::isInstance)
                .map(Produttore.class::cast);
    }



    // Dashboard del produttore
    @GetMapping("/dashboard")
    public String dashboardProduttore(Model model,
                                      HttpSession session,
                                      @RequestParam(name = "toggleForm", required = false) Boolean toggleForm,
                                      @RequestParam(name = "success", required = false) Boolean success) {
        Optional<Produttore> produttoreOpt = getProduttoreFromSession(session);
        if (produttoreOpt.isEmpty()) return "redirect:/login";

        Produttore produttore = produttoreOpt.get();
        model.addAttribute("messaggio", produttore.messaggioDashboard());
        model.addAttribute("prodotto", new ProdottoForm());

        if (Boolean.TRUE.equals(toggleForm)) model.addAttribute("mostraForm", true);
        if (Boolean.TRUE.equals(success)) model.addAttribute("successo", true);

        List<Prodotto> prodotti = prodottoRepository.findByProduttore(produttore).stream()
                .filter(p -> p.getStato() == Prodotto.StatoProdotto.IN_BOZZA
                        || p.getStato() == Prodotto.StatoProdotto.RIFIUTATO)
                .toList();

        model.addAttribute("prodotti", prodotti);
        return "produttore_dashboard";
    }

    // Creazione nuovo prodotto
    @PostMapping("/crea-prodotto")
    public String salvaProdotto(@ModelAttribute ProdottoForm prodottoForm, HttpSession session) throws IOException {
        Optional<Produttore> produttoreOpt = getProduttoreFromSession(session);
        if (produttoreOpt.isEmpty()) return "redirect:/login";

        Produttore produttore = produttoreOpt.get();

        // 1. Salva il prodotto
        Prodotto prodotto = prodottoForm.toProdotto(produttore);
        prodotto.setStato(Prodotto.StatoProdotto.IN_ATTESA_APPROVAZIONE);
        prodotto = prodottoRepository.save(prodotto);

        // 2. Salva immagini
        Path imgDir = Paths.get("uploads/immagini");
        Files.createDirectories(imgDir);

        for (MultipartFile img : Optional.ofNullable(prodottoForm.getImmagini()).orElse(List.of())) {
            if (!img.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + img.getOriginalFilename();
                img.transferTo(imgDir.resolve(fileName));

                ImmagineProdotto ip = new ImmagineProdotto();
                ip.setFileName(fileName);
                ip.setProdotto(prodotto);
                immagineRepo.save(ip);
            }
        }

        // 3. Salva certificati
        Path certDir = Paths.get("uploads/certificati");
        Files.createDirectories(certDir);

        for (MultipartFile cert : Optional.ofNullable(prodottoForm.getCertificati()).orElse(List.of())) {
            if (!cert.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + cert.getOriginalFilename();
                cert.transferTo(certDir.resolve(fileName));

                CertificatoProdotto cp = new CertificatoProdotto();
                cp.setFileName(fileName);
                cp.setProdotto(prodotto);
                certificatoRepo.save(cp);
            }
        }

        return "redirect:/produttore/dashboard?success=true";
    }

    // Re-invia al curatore un prodotto in bozza/rifiutato
    @PostMapping("/invia-al-curatore/{id}")
    public String inviaAlCuratore(@PathVariable Long id, HttpSession session) {
        Optional<Produttore> produttoreOpt = getProduttoreFromSession(session);
        if (produttoreOpt.isEmpty()) return "redirect:/login";

        Produttore produttore = produttoreOpt.get();

        prodottoRepository.findById(id).ifPresent(prodotto -> {
            if (prodotto.getProduttore().equals(produttore)) {
                prodotto.setStato(Prodotto.StatoProdotto.IN_ATTESA_APPROVAZIONE);
                prodotto.setCommentoRifiuto(null); // reset eventuale commento rifiuto
                prodottoRepository.save(prodotto);
            }
        });

        return "redirect:/produttore/dashboard";
    }
}
