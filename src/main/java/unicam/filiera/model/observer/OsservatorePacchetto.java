package unicam.filiera.model.observer;

import unicam.filiera.model.Pacchetto;

/**
 * Interfaccia per gli osservatori di eventi sui pacchetti.
 */
public interface OsservatorePacchetto {
    void notifica(Pacchetto pacchetto, String evento);
}
