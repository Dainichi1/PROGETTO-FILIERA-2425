package unicam.filiera.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AutenticazioneController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if ("credenziali".equals(error)) {
            model.addAttribute("error", "⚠ Credenziali non valide.");
        } else if ("ruolo".equals(error)) {
            model.addAttribute("error", "⚠ Devi selezionare il ruolo corretto per questo utente.");
        }
        return "login";
    }
}
