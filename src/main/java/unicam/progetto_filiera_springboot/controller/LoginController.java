// src/main/java/unicam/progetto_filiera_springboot/controller/LoginController.java
package unicam.progetto_filiera_springboot.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import unicam.progetto_filiera_springboot.dto.auth.LoginDto;
import unicam.progetto_filiera_springboot.domain.model.Ruolo;
import unicam.progetto_filiera_springboot.domain.actor.UtenteAutenticato; // <-- attore dominio
import unicam.progetto_filiera_springboot.application.service.AuthService;
import unicam.progetto_filiera_springboot.strategy.validation.ValidationException;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Controller
@RequestMapping("/login")
public class LoginController {

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("dto", new LoginDto());
        model.addAttribute("ruoli", Ruolo.values());
        return "login/login";
    }

    @PostMapping
    public String submit(@ModelAttribute("dto") LoginDto dto, Model model, HttpSession session) {
        try {
            UtenteAutenticato attore = authService.loginAsActor(dto);

            // 1) Salva comunque se ti serve altrove (non più necessario per i Controller con Principal)
            session.setAttribute("attore", attore);

            // 2) Crea Authentication con authority derivata dal ruolo
            var authority = new SimpleGrantedAuthority("ROLE_" + attore.getRuolo().name());
            var authentication = new UsernamePasswordAuthenticationToken(attore.getUsername(), null, java.util.List.of(authority));

            // 3) Metti nel SecurityContext e persisti in sessione
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, context);

            // 4) Redirect per ruolo (come prima)
            switch (attore.getRuolo()) {
                case PRODUTTORE -> { return "redirect:/produttore"; }
                case TRASFORMATORE -> { return "redirect:/trasformatore"; }
                case DISTRIBUTORE_TIPICITA -> { return "redirect:/distributore"; }
                case CURATORE -> { return "redirect:/curatore"; }
                case ANIMATORE -> { return "redirect:/animatore"; }
                case GESTORE_PIATTAFORMA -> { return "redirect:/gestore"; }
                case ACQUIRENTE -> { return "redirect:/acquirente"; }
            }

            model.addAttribute("success", "Benvenuto " + attore.getNome() + " (" + attore.getRuolo() + ")");
            model.addAttribute("ruoli", Ruolo.values());
            return "login/login";

        } catch (ValidationException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("ruoli", Ruolo.values());
            return "login/login";
        }
    }
}
