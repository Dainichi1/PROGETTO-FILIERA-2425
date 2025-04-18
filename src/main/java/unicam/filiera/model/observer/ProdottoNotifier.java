package unicam.filiera.model.observer;

import unicam.filiera.model.Prodotto;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che funge da soggetto osservato nel pattern Observer.
 * Notifica tutti gli osservatori registrati quando un prodotto viene aggiornato.
 */
public class ProdottoNotifier {

    private static ProdottoNotifier instance;

    private final List<OsservatoreProdotto> osservatori = new ArrayList<>();

    private ProdottoNotifier() {
    }

    public static ProdottoNotifier getInstance() {
        if (instance == null) {
            instance = new ProdottoNotifier();
        }
        return instance;
    }

    public void registraOsservatore(OsservatoreProdotto osservatore) {
        osservatori.add(osservatore);
    }

    public void notificaTutti(Prodotto prodotto, String evento) {
        for (OsservatoreProdotto o : osservatori) {
            o.notifica(prodotto, evento);
        }
    }

    public void rimuoviOsservatore(OsservatoreProdotto osservatore) {
        osservatori.remove(osservatore);
    }
}
