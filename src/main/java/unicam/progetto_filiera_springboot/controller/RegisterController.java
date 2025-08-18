package unicam.progetto_filiera_springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import unicam.progetto_filiera_springboot.dto.auth.RegisterDto;
import unicam.progetto_filiera_springboot.service.AuthService;
import unicam.progetto_filiera_springboot.strategy.validation.ValidationException;
import unicam.progetto_filiera_springboot.domain.model.Ruolo;

@Controller
@RequestMapping("/register")
public class RegisterController {

    private final AuthService authService;

    public RegisterController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("dto", new RegisterDto());
        model.addAttribute("ruoli", Ruolo.values());
        return "register/register";
    }

    @PostMapping
    public String submit(@ModelAttribute("dto") RegisterDto dto, Model model) {
        try {
            String msg = authService.register(dto);
            model.addAttribute("success", msg); // "Registrazione completata!"
        } catch (ValidationException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        model.addAttribute("ruoli", Ruolo.values());
        return "register/register";
    }
}
