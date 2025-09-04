package unicam.filiera.observer;

import unicam.filiera.model.Prodotto;

/**
 * Interfaccia per gli osservatori di eventi sui prodotti.
 */
public interface OsservatoreProdotto {
    void notifica(Prodotto prodotto, String evento);
}
