package unicam.filiera.observer;

import unicam.filiera.model.Fiera;

/**
 * Interfaccia per gli osservatori di eventi sulle fiere.
 */
public interface OsservatoreFiera {
    void notifica(Fiera fiera, String evento);
}
