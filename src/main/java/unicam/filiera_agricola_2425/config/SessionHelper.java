package unicam.filiera_agricola_2425.config;

import jakarta.servlet.http.HttpSession;
import unicam.filiera_agricola_2425.models.Ruolo;
import unicam.filiera_agricola_2425.models.UtenteAutenticato;
import unicam.filiera_agricola_2425.repositories.UtenteRepository;

import java.util.Optional;

public class SessionHelper {

    public static Optional<UtenteAutenticato> getUtenteAutenticato(HttpSession session, UtenteRepository repo, Ruolo ruoloAtteso) {
        String username = (String) session.getAttribute("username");
        Object ruoloObj = session.getAttribute("ruolo");

        if (username == null || ruoloObj == null) return Optional.empty();
        if (!(ruoloObj instanceof Ruolo ruolo) || ruolo != ruoloAtteso) return Optional.empty();

        return repo.findByUsername(username);
    }
}
