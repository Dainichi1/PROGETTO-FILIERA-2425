package unicam.filiera.service;

import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.dto.PacchettoViewDto;
import unicam.filiera.entity.PacchettoEntity;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.StatoProdotto;

import java.util.List;
import java.util.Optional;

public interface PacchettoService {

    void creaPacchetto(PacchettoDto dto, String creatore);

    /**
     * Aggiorna un pacchetto rifiutato (identificato dall'ID).
     *
     * @param id ID del pacchetto da aggiornare
     * @param dto DTO con i nuovi dati
     * @param creatore username del distributore che lo possiede
     */
    void aggiornaPacchetto(Long id, PacchettoDto dto, String creatore);

    List<Pacchetto> getPacchettiCreatiDa(String creatore);

    List<Pacchetto> getPacchettiByStato(StatoProdotto stato);

    /**
     * Elimina un pacchetto usando l'ID (solo se non è APPROVATO e appartiene al distributore).
     *
     * @param id id del pacchetto
     * @param creatore username del distributore
     */
    void eliminaPacchettoById(Long id, String creatore);

    /**
     * Permette al Curatore di aggiornare lo stato e il commento di un pacchetto.
     *
     * @param nome nome del pacchetto
     * @param creatore username del distributore
     * @param nuovoStato nuovo stato (APPROVATO o RIFIUTATO)
     * @param commento eventuale commento (solo in caso di RIFIUTO)
     */
    void cambiaStatoPacchetto(String nome, String creatore, StatoProdotto nuovoStato, String commento);

    /**
     * Trova un PacchettoEntity per ID (serve per pre-popolare il form in modifica).
     *
     * @param id id del pacchetto
     * @return Optional con l’entity se trovato
     */
    Optional<PacchettoEntity> findEntityById(Long id);

    List<PacchettoViewDto> getPacchettiViewByStato(StatoProdotto stato);

    List<PacchettoViewDto> getPacchettiViewByCreatore(String creatore);

}
