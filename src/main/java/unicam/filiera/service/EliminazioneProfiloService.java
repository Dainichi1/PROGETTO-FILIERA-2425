package unicam.filiera.service;

import unicam.filiera.dto.RichiestaEliminazioneProfiloDto;
import unicam.filiera.entity.RichiestaEliminazioneProfiloEntity;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;

import java.util.List;
import java.util.Optional;

public interface EliminazioneProfiloService {

    /**
     * Invia una nuova richiesta di eliminazione profilo.
     */
    void inviaRichiestaEliminazione(RichiestaEliminazioneProfiloDto dto);

    /**
     * Restituisce tutte le richieste con lo stato specificato (in formato DTO).
     */
    List<RichiestaEliminazioneProfiloDto> getRichiesteByStato(StatoRichiestaEliminazioneProfilo stato);

    /**
     * Restituisce tutte le richieste fatte da un certo utente (in formato DTO).
     */
    List<RichiestaEliminazioneProfiloDto> getRichiesteByUtente(String username);

    /**
     * Recupera una richiesta come Entity (per logiche interne o transaction).
     */
    Optional<RichiestaEliminazioneProfiloEntity> findEntityById(Long id);

    /**
     * Aggiorna lo stato di una richiesta.
     */
    void aggiornaStato(Long id, StatoRichiestaEliminazioneProfilo nuovoStato);
}
