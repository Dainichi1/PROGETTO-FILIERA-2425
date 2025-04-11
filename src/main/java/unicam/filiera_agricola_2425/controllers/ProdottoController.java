package unicam.filiera_agricola_2425.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import unicam.filiera_agricola_2425.config.SessionHelper;
import unicam.filiera_agricola_2425.dtos.ProdottoDashboardDTO;
import unicam.filiera_agricola_2425.dtos.ProdottoForm;
import unicam.filiera_agricola_2425.models.*;
import unicam.filiera_agricola_2425.repositories.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
@RequestMapping("/produttore")
public class ProdottoController {

    @Autowired private ProdottoRepository prodottoRepository;
    @Autowired private UtenteRepository utenteRepository;
    @Autowired private ImmagineProdottoRepository immagineRepo;
    @Autowired private CertificatoProdottoRepository certificatoRepo;

    @GetMapping("/dashboard")
    public String dashboardProduttore(Model model,
                                      HttpSession session,
                                      @RequestParam(name = "toggleForm", required = false) Boolean toggleForm,
                                      @RequestParam(name = "success", required = false) Boolean success) {

        Optional<UtenteAutenticato> utenteOpt = SessionHelper.getUtenteAutenticato(session, utenteRepository, Ruolo.PRODUTTORE);
        // DEBUG
        System.out.println("DEBUG - utenteOpt.isPresent(): " + utenteOpt.isPresent());
        utenteOpt.ifPresent(u -> {
            System.out.println("DEBUG - Classe effettiva: " + u.getClass().getName());
            System.out.println("DEBUG - Ruolo in DB: " + u.getRuolo());
            System.out.println("DEBUG - username in DB: " + u.getUsername());
        });
        // FINE DEBUG
        if (utenteOpt.isEmpty() || !(utenteOpt.get() instanceof Produttore produttore)) {
            return "redirect:/login";
        }

        System.out.println("DEBUG - Sono dopo il check instanceof Produttore, non reindirizzo più al login!");


        model.addAttribute("messaggio", produttore.messaggioDashboard());
        model.addAttribute("prodotto", new ProdottoForm());

        if (Boolean.TRUE.equals(toggleForm)) model.addAttribute("mostraForm", true);
        if (Boolean.TRUE.equals(success)) model.addAttribute("successo", true);

        List<ProdottoDashboardDTO> prodottiDTO = prodottoRepository.findByProduttore(produttore).stream()
                .filter(p -> p.getStato() == Prodotto.StatoProdotto.IN_BOZZA ||
                        p.getStato() == Prodotto.StatoProdotto.RIFIUTATO)
                .map(ProdottoDashboardDTO::fromModel)
                .toList();

        model.addAttribute("prodotti", prodottiDTO);
        return "produttore_dashboard";
    }

    @PostMapping("/crea-prodotto")
    public String salvaProdotto(@ModelAttribute ProdottoForm prodottoForm, HttpSession session) throws IOException {
        Optional<UtenteAutenticato> utenteOpt = SessionHelper.getUtenteAutenticato(session, utenteRepository, Ruolo.PRODUTTORE);
        if (utenteOpt.isEmpty() || !(utenteOpt.get() instanceof Produttore produttore)) {
            return "redirect:/login";
        }

        Prodotto prodotto = prodottoForm.toProdotto(produttore);
        prodotto.setStato(Prodotto.StatoProdotto.IN_ATTESA_APPROVAZIONE);
        prodotto = prodottoRepository.save(prodotto);

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

    @PostMapping("/invia-al-curatore/{id}")
    public String inviaAlCuratore(@PathVariable Long id, HttpSession session) {
        Optional<UtenteAutenticato> utenteOpt = SessionHelper.getUtenteAutenticato(session, utenteRepository, Ruolo.PRODUTTORE);
        if (utenteOpt.isEmpty() || !(utenteOpt.get() instanceof Produttore produttore)) {
            return "redirect:/login";
        }

        prodottoRepository.findById(id).ifPresent(prodotto -> {
            if (prodotto.getProduttore().equals(produttore)) {
                prodotto.setStato(Prodotto.StatoProdotto.IN_ATTESA_APPROVAZIONE);
                prodotto.setCommentoRifiuto(null);
                prodottoRepository.save(prodotto);
            }
        });

        return "redirect:/produttore/dashboard";
    }
}
