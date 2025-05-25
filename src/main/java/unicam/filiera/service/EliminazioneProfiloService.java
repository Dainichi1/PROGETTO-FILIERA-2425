package unicam.filiera.service;

import unicam.filiera.dto.RichiestaEliminazioneProfiloDto;
import unicam.filiera.model.RichiestaEliminazioneProfilo;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;

import java.util.List;

/**
 * Service per la gestione delle richieste di eliminazione profilo.
 */
public interface EliminazioneProfiloService {
    /**
     * Invia una nuova richiesta di eliminazione profilo (stato IN_ATTESA).
     * Lancia IllegalStateException se esiste già una richiesta IN_ATTESA per quell'utente.
     */
    void inviaRichiestaEliminazione(RichiestaEliminazioneProfiloDto dto);

    /**
     * Restituisce tutte le richieste di eliminazione con un certo stato.
     */
    List<RichiestaEliminazioneProfilo> getRichiesteByStato(StatoRichiestaEliminazioneProfilo stato);

    /**
     * Restituisce tutte le richieste di eliminazione fatte da un utente.
     */
    List<RichiestaEliminazioneProfilo> getRichiesteByUtente(String username);
}
