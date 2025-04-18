package unicam.filiera.controller;

import unicam.filiera.model.observer.ProdottoNotifier;
import unicam.filiera.model.observer.OsservatoreProdotto;

/**
 * Classe helper per gestire la registrazione e rimozione degli osservatori
 * nel pattern Observer, mantenendo separata la logica dalla vista.
 */
public class ObserverManager {

    /**
     * Registra un nuovo osservatore che riceverà notifiche sui prodotti.
     */
    public static void registraOsservatore(OsservatoreProdotto osservatore) {
        ProdottoNotifier.getInstance().registraOsservatore(osservatore);
    }

    /**
     * Rimuove un osservatore precedentemente registrato.
     */
    public static void rimuoviOsservatore(OsservatoreProdotto osservatore) {
        ProdottoNotifier.getInstance().rimuoviOsservatore(osservatore);
    }
}
