package unicam.filiera.observer;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import unicam.filiera.model.ProdottoTrasformato;

import java.util.List;

/**
 * Gestisce la notifica degli eventi ai vari osservatori di Prodotto Trasformato.
 * Gli osservatori vengono registrati automaticamente come bean Spring.
 */
@Component
public class ProdottoTrasformatoNotifier {

    private final List<OsservatoreProdottoTrasformato> osservatori;

    // Spring inietta automaticamente tutti i bean che implementano OsservatoreProdottoTrasformato
    public ProdottoTrasformatoNotifier(List<OsservatoreProdottoTrasformato> osservatori) {
        this.osservatori = osservatori;
    }

    @PostConstruct
    public void init() {
        System.out.println("ProdottoTrasformatoNotifier inizializzato con "
                + osservatori.size() + " osservatori registrati.");
    }

    public void notificaTutti(ProdottoTrasformato prodottoTrasformato, String evento) {
        for (OsservatoreProdottoTrasformato o : osservatori) {
            o.notifica(prodottoTrasformato, evento);
        }
    }
}
