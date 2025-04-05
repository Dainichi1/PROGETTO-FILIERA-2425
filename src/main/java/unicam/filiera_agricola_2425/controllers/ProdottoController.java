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
import java.nio.file.Path;



import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

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
    public String salvaProdotto(@ModelAttribute ProdottoForm prodottoForm, HttpSession session) throws IOException {
        String username = (String) session.getAttribute("username");
        if (username == null) return "redirect:/login";

        Optional<UtenteAutenticato> utenteOpt = utenteRepository.findByUsername(username);
        if (utenteOpt.isEmpty() || !(utenteOpt.get() instanceof Produttore produttore)) {
            return "redirect:/login";
        }

        // 1. Salva il prodotto base
        Prodotto prodotto = prodottoForm.toProdotto(produttore);
        prodotto = prodottoRepository.save(prodotto);

        // 2. Salva immagini
        if (prodottoForm.getImmagini() != null) {
            Path imgDir = Paths.get("uploads/immagini");
            Files.createDirectories(imgDir);

            for (MultipartFile img : prodottoForm.getImmagini()) {
                if (!img.isEmpty()) {
                    String fileName = UUID.randomUUID() + "_" + img.getOriginalFilename();
                    img.transferTo(imgDir.resolve(fileName));

                    ImmagineProdotto ip = new ImmagineProdotto();
                    ip.setFileName(fileName);
                    ip.setProdotto(prodotto);
                    immagineRepo.save(ip);
                }
            }
        }

        // 3. Salva certificati
        if (prodottoForm.getCertificati() != null) {
            Path certDir = Paths.get("uploads/certificati");
            Files.createDirectories(certDir);

            for (MultipartFile cert : prodottoForm.getCertificati()) {
                if (!cert.isEmpty()) {
                    String fileName = UUID.randomUUID() + "_" + cert.getOriginalFilename();
                    cert.transferTo(certDir.resolve(fileName));

                    CertificatoProdotto cp = new CertificatoProdotto();
                    cp.setFileName(fileName);
                    cp.setProdotto(prodotto);
                    certificatoRepo.save(cp);
                }
            }
        }

        return "redirect:/produttore/dashboard?success=true";
    }
}
