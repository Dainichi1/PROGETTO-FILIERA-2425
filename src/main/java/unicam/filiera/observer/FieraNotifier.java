package unicam.filiera.observer;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import unicam.filiera.model.Fiera;

import java.util.List;

/**
 * Gestisce la notifica degli eventi ai vari osservatori di fiere.
 */
@Component
public class FieraNotifier {

    private final List<OsservatoreFiera> osservatori;

    // Spring inietta automaticamente tutti i bean che implementano OsservatoreFiera
    public FieraNotifier(List<OsservatoreFiera> osservatori) {
        this.osservatori = osservatori;
    }

    @PostConstruct
    public void init() {
        System.out.println("FieraNotifier inizializzato con " + osservatori.size() + " osservatori registrati.");
    }

    public void notificaTutti(Fiera fiera, String evento) {
        for (OsservatoreFiera o : osservatori) {
            o.notifica(fiera, evento);
        }
    }
}
