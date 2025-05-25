package unicam.filiera.controller;

import unicam.filiera.dto.RichiestaEliminazioneProfiloDto;
import unicam.filiera.model.RichiestaEliminazioneProfilo;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;
import unicam.filiera.service.EliminazioneProfiloService;
import unicam.filiera.service.EliminazioneProfiloServiceImpl;
import unicam.filiera.dao.JdbcRichiestaEliminazioneProfiloDAO;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Controller per la gestione delle richieste di eliminazione profilo da parte di un utente autenticato.
 */
public class EliminazioneProfiloController {

    private final String username;
    private final EliminazioneProfiloService service;

    public EliminazioneProfiloController(String username, EliminazioneProfiloService service) {
        this.username = username;
        this.service = service;
    }

    public EliminazioneProfiloController(String username) {
        this(username, new EliminazioneProfiloServiceImpl(JdbcRichiestaEliminazioneProfiloDAO.getInstance()));
    }

    /**
     * Invoca il flusso di richiesta eliminazione profilo.
     * Usa un callback per notificare la view del risultato (success/fail, messaggio).
     */
    public void inviaRichiestaEliminazione(BiConsumer<Boolean, String> callback) {
        try {
            RichiestaEliminazioneProfiloDto dto = new RichiestaEliminazioneProfiloDto(username);
            service.inviaRichiestaEliminazione(dto);
            callback.accept(true, "Richiesta di eliminazione profilo inviata! Attendi l'approvazione del Gestore.");
        } catch (IllegalStateException ise) {
            callback.accept(false, ise.getMessage());
        } catch (Exception ex) {
            callback.accept(false, "Errore imprevisto: " + ex.getMessage());
        }
    }

    /**
     * Recupera tutte le richieste di eliminazione inviate dall'utente.
     */
    public List<RichiestaEliminazioneProfilo> getMieRichieste() {
        return service.getRichiesteByUtente(username);
    }

    /**
     * Recupera tutte le richieste di eliminazione con stato specifico (IN_ATTESA, APPROVATA).
     * Utile per il gestore in futuro.
     */
    public List<RichiestaEliminazioneProfilo> getRichiesteByStato(StatoRichiestaEliminazioneProfilo stato) {
        return service.getRichiesteByStato(stato);
    }
}
