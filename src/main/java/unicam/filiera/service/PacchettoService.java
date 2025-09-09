package unicam.filiera.service;

import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.StatoProdotto;

import java.util.List;

public interface PacchettoService {

    void creaPacchetto(PacchettoDto dto, String creatore);

    void aggiornaPacchetto(String nomeOriginale, PacchettoDto dto, String creatore);

    List<Pacchetto> getPacchettiCreatiDa(String creatore);

    List<Pacchetto> getPacchettiByStato(StatoProdotto stato);

    void eliminaPacchetto(String nome, String creatore);

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
     * Elimina un pacchetto usando l'ID (solo se non è APPROVATO e appartiene al distributore).
     *
     * @param id id del pacchetto
     * @param creatore username del distributore
     */
    void eliminaPacchettoById(Long id, String creatore);
}
