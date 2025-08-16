package unicam.filiera.controller;

import unicam.filiera.model.observer.OsservatoreProdotto;
import unicam.filiera.model.observer.ProdottoNotifier;

/**
 * Helper per registrare e rimuovere {@link OsservatoreProdotto},
 * mantenendo la logica di sottoscrizione separata dagli strati di vista.
 */
public class ObserverManagerProdotto {

    /**
     * Registra un osservatore che riceverà gli eventi sui prodotti.
     */
    public static void registraOsservatore(OsservatoreProdotto o) {
        ProdottoNotifier.getInstance().registraOsservatore(o);
    }

    /**
     * Rimuove un osservatore precedentemente registrato.
     */
    public static void rimuoviOsservatore(OsservatoreProdotto o) {
        ProdottoNotifier.getInstance().rimuoviOsservatore(o);
    }
}
