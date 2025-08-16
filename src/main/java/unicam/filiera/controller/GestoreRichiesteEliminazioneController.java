// src/main/java/unicam/filiera/controller/GestoreRichiesteEliminazioneController.java
package unicam.filiera.controller;

import unicam.filiera.dao.JdbcRichiestaEliminazioneProfiloDAO;
import unicam.filiera.dao.JdbcUtenteDAO;
import unicam.filiera.model.RichiestaEliminazioneProfilo;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;
import unicam.filiera.model.observer.EliminazioneProfiloNotifier;
import unicam.filiera.service.EliminazioneProfiloService;
import unicam.filiera.service.EliminazioneProfiloServiceImpl;

import java.util.List;

public class GestoreRichiesteEliminazioneController {

    private final EliminazioneProfiloService service;

    public GestoreRichiesteEliminazioneController() {
        this.service = new EliminazioneProfiloServiceImpl(JdbcRichiestaEliminazioneProfiloDAO.getInstance());
    }

    /**
     * Step 3: elenco richieste IN_ATTESA.
     */
    public List<RichiestaEliminazioneProfilo> getRichiesteInAttesa() {
        return service.getRichiesteByStato(StatoRichiestaEliminazioneProfilo.IN_ATTESA);
    }

    /**
     * Step 5: dettaglio richiesta.
     */
    public RichiestaEliminazioneProfilo getDettaglio(int richiestaId) {
        return JdbcRichiestaEliminazioneProfiloDAO.getInstance().findById(richiestaId);
    }

    /**
     * Step 6.a.1: rifiuta richiesta + notifica utente.
     */
    public boolean rifiutaRichiesta(int richiestaId) {
        var dao = JdbcRichiestaEliminazioneProfiloDAO.getInstance();
        RichiestaEliminazioneProfilo r = dao.findById(richiestaId);
        if (r == null) return false;

        boolean ok = dao.updateStato(richiestaId, StatoRichiestaEliminazioneProfilo.RIFIUTATA);
        if (ok) {
            EliminazioneProfiloNotifier.getInstance()
                    .notificaRifiutata(r.getUsername(), richiestaId, "La richiesta è stata rifiutata dal gestore.");
        }
        return ok;
    }

    /**
     * Step 7-10: approva richiesta -> (7) messaggio di conferma lato UI,
     * (9) elimina utente, (10) aggiorna stato APPROVATA, (11) notifica utente.
     * Nota: qui manteniamo lo storico nella tabella richieste.
     */
    public boolean approvaRichiesta(int richiestaId) {
        var dao = JdbcRichiestaEliminazioneProfiloDAO.getInstance();
        RichiestaEliminazioneProfilo r = dao.findById(richiestaId);
        if (r == null) return false;

        // (10) stato = APPROVATA (storico mantenuto)
        boolean statoAggiornato = dao.updateStato(richiestaId, StatoRichiestaEliminazioneProfilo.APPROVATA);
        if (!statoAggiornato) return false;

        // (9) elimina profilo utente
        boolean deleted = JdbcUtenteDAO.getInstance().deleteByUsername(r.getUsername());
        if (!deleted) {
            // piccolo rollback logico: riporto in ATTESA se la delete fallisce
            dao.updateStato(richiestaId, StatoRichiestaEliminazioneProfilo.IN_ATTESA);
            return false;
        }

        // (11) notifica utente dell’avvenuta eliminazione
        EliminazioneProfiloNotifier.getInstance()
                .notificaEliminata(r.getUsername(), richiestaId);

        return true;
    }
}
