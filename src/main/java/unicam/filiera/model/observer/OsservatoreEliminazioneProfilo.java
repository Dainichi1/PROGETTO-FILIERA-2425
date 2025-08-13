package unicam.filiera.model.observer;

public interface OsservatoreEliminazioneProfilo {
    /** Richiesta rifiutata dal Gestore. */
    void onRichiestaRifiutata(String username, int richiestaId, String motivo);

    /** Richiesta approvata e profilo eliminato. */
    void onProfiloEliminato(String username, int richiestaId);
}
