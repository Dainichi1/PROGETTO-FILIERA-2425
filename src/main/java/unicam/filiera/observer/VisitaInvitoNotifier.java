package unicam.filiera.observer;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import unicam.filiera.model.VisitaInvito;

import java.util.List;

/**
 * Gestisce la notifica degli eventi ai vari osservatori di visite.
 */
@Component
public class VisitaInvitoNotifier {

    private final List<OsservatoreVisitaInvito> osservatori;

    // Spring inietta automaticamente tutti i bean che implementano OsservatoreVisitaInvito
    public VisitaInvitoNotifier(List<OsservatoreVisitaInvito> osservatori) {
        this.osservatori = osservatori;
    }

    @PostConstruct
    public void init() {
        System.out.println("VisitaInvitoNotifier inizializzato con " + osservatori.size() + " osservatori registrati.");
    }

    public void notificaTutti(VisitaInvito visita, String evento) {
        for (OsservatoreVisitaInvito o : osservatori) {
            o.notifica(visita, evento);
        }
    }
}
