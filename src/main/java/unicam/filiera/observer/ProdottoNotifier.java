package unicam.filiera.observer;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Gestisce la notifica degli eventi ai vari osservatori.
 * Gli osservatori vengono registrati automaticamente come bean Spring.
 */
@Component
public class ProdottoNotifier {

    private final List<OsservatoreProdotto> osservatori;

    // Spring inietta automaticamente tutti i bean che implementano OsservatoreProdotto
    public ProdottoNotifier(List<OsservatoreProdotto> osservatori) {
        this.osservatori = osservatori;
    }

    @PostConstruct
    public void init() {
        System.out.println("ProdottoNotifier inizializzato con " + osservatori.size() + " osservatori registrati.");
    }

    public void notificaTutti(unicam.filiera.model.Prodotto prodotto, String evento) {
        for (OsservatoreProdotto o : osservatori) {
            o.notifica(prodotto, evento);
        }
    }
}
