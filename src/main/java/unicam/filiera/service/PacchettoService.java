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

    /** Aggiorna un pacchetto rifiutato (identificato dall'ID). */
    void aggiornaPacchetto(Long id, PacchettoDto dto, String creatore);

    List<Pacchetto> getPacchettiCreatiDa(String creatore);

    List<Pacchetto> getPacchettiByStato(StatoProdotto stato);

    /** Permette al Curatore di aggiornare lo stato e il commento di un pacchetto. */
    void cambiaStatoPacchetto(String nome, String creatore, StatoProdotto nuovoStato, String commento);

    /** Trova una entity per ID (serve per pre-popolare form o per i controlli centrali). */
    Optional<PacchettoEntity> findEntityById(Long id);

    List<PacchettoViewDto> getPacchettiViewByStato(StatoProdotto stato);

    List<PacchettoViewDto> getPacchettiViewByCreatore(String creatore);

    /**
     * Elimina un pacchetto applicando le policy di dominio:
     * - creatoDa == username
     * - stato != APPROVATO (ammessi IN_ATTESA o RIFIUTATO)
     * Emette la notifica di eliminazione.
     */
    void eliminaById(Long id, String username);
}
