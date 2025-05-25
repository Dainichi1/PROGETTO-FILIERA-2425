package unicam.filiera.model.observer;

import unicam.filiera.model.ProdottoTrasformato;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che funge da soggetto osservato (Subject) nel pattern Observer.
 * Notifica tutti gli osservatori registrati quando un prodotto trasformato viene aggiornato.
 */
public class ProdottoTrasformatoNotifier {

    private static ProdottoTrasformatoNotifier instance;

    private final List<OsservatoreProdottoTrasformato> osservatori = new ArrayList<>();

    private ProdottoTrasformatoNotifier() {
    }

    public static ProdottoTrasformatoNotifier getInstance() {
        if (instance == null) {
            instance = new ProdottoTrasformatoNotifier();
        }
        return instance;
    }

    public void registraOsservatore(OsservatoreProdottoTrasformato osservatore) {
        osservatori.add(osservatore);
        System.out.println("[NOTIFIER] Aggiunto osservatore: " + osservatore + " - TOT: " + osservatori.size());
    }

    public void notificaTutti(ProdottoTrasformato prodotto, String evento) {
        System.out.println("[NOTIFIER] Notifica evento '" + evento + "' per " + prodotto.getNome() + " a " + osservatori.size() + " osservatori");
        for (OsservatoreProdottoTrasformato o : osservatori) {
            o.notifica(prodotto, evento);
        }
    }

    public void rimuoviOsservatore(OsservatoreProdottoTrasformato osservatore) {
        osservatori.remove(osservatore);
        System.out.println("[NOTIFIER] Rimosso osservatore: " + osservatore + " - TOT: " + osservatori.size());
    }


}
