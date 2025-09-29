package unicam.filiera.observer;

import unicam.filiera.model.ProdottoTrasformato;

/**
 * Interfaccia per gli osservatori di eventi sui prodotti trasformati.
 */
public interface OsservatoreProdottoTrasformato {
    void notifica(ProdottoTrasformato prodottoTrasformato, String evento);
}
