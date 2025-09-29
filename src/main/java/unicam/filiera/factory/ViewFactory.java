package unicam.filiera.factory;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import unicam.filiera.model.Ruolo;
import unicam.filiera.model.UtenteAutenticato;

import java.util.EnumMap;
import java.util.Map;

@Component
public class ViewFactory {

    @FunctionalInterface
    public interface ViewStrategy {
        String render(Model model, UtenteAutenticato utente);
    }

    private final Map<Ruolo, ViewStrategy> registry = new EnumMap<>(Ruolo.class);
    private final ViewStrategy fallbackStrategy;

    public ViewFactory() {
        // Strategie già pronte
        registry.put(Ruolo.ACQUIRENTE, (model, u) -> {
            model.addAttribute("utente", u);
            return "dashboard/acquirente";
        });

        registry.put(Ruolo.PRODUTTORE, (model, u) -> {
            model.addAttribute("utente", u);
            return "dashboard/produttore";
        });

        registry.put(Ruolo.CURATORE, (model, u) -> {
            model.addAttribute("utente", u);
            return "dashboard/curatore";
        });

        // fallback generico
        fallbackStrategy = (model, u) -> {
            model.addAttribute("utente", u);
            return "dashboard/generico";
        };
    }

    public String viewFor(Ruolo ruolo, Model model, UtenteAutenticato utente) {
        return registry.getOrDefault(ruolo, fallbackStrategy).render(model, utente);
    }

    // estensione runtime (Strategy registrabile)
    public void register(Ruolo ruolo, ViewStrategy strategy) {
        registry.put(ruolo, strategy);
    }
}
