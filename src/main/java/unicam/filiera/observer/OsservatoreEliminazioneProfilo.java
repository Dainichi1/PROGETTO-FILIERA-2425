package unicam.filiera.observer;

import unicam.filiera.model.RichiestaEliminazioneProfilo;

/**
 * Interfaccia per osservatori delle richieste di eliminazione profilo.
 */
public interface OsservatoreEliminazioneProfilo {
    void notifica(RichiestaEliminazioneProfilo richiesta, String evento, String messaggio);
}
