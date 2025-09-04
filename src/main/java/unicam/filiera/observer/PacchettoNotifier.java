package unicam.filiera.observer;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import unicam.filiera.model.Pacchetto;

import java.util.List;

/**
 * Gestisce la notifica degli eventi ai vari osservatori di Pacchetto.
 * Gli osservatori vengono registrati automaticamente come bean Spring.
 */
@Component
public class PacchettoNotifier {

    private final List<OsservatorePacchetto> osservatori;

    // Spring inietta automaticamente tutti i bean che implementano OsservatorePacchetto
    public PacchettoNotifier(List<OsservatorePacchetto> osservatori) {
        this.osservatori = osservatori;
    }

    @PostConstruct
    public void init() {
        System.out.println("PacchettoNotifier inizializzato con " + osservatori.size() + " osservatori registrati.");
    }

    public void notificaTutti(Pacchetto pacchetto, String evento) {
        for (OsservatorePacchetto o : osservatori) {
            o.notifica(pacchetto, evento);
        }
    }
}
