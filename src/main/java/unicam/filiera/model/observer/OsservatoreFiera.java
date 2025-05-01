package unicam.filiera.model.observer;

import unicam.filiera.model.Fiera;

/**
 * Interfaccia per chi vuole ricevere notifiche sugli eventi legati alle fiere.
 */
public interface OsservatoreFiera {
    /**
     * Chiamato quando una fiera cambia stato o viene creata.
     *
     * @param fiera   l’evento Fiera interessato
     * @param evento  una stringa che identifica l’azione, es. "NUOVA_FIERA", "FIERA_PUBBLICATA", "FIERA_RIMOSSA"
     */
    void notifica(Fiera fiera, String evento);
}
