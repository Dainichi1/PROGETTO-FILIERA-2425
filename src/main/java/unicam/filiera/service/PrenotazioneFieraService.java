package unicam.filiera.service;

import unicam.filiera.dto.PrenotazioneFieraDto;
import unicam.filiera.entity.PrenotazioneFieraEntity;

import java.util.List;
import java.util.Optional;

public interface PrenotazioneFieraService {

    /**
     * Crea una nuova prenotazione per una fiera e scala i fondi dell’acquirente.
     *
     * @param dto DTO contenente idFiera e numeroPersone
     * @param usernameAcquirente username autenticato dell’acquirente
     * @return i fondi rimanenti dopo la prenotazione
     * @throws IllegalStateException se l’utente ha già prenotato
     * @throws IllegalArgumentException se i fondi sono insufficienti
     */
    double creaPrenotazione(PrenotazioneFieraDto dto, String usernameAcquirente);

    /**
     * Recupera tutte le prenotazioni fatte da un acquirente.
     */
    List<PrenotazioneFieraDto> getPrenotazioniByAcquirente(String usernameAcquirente);

    /**
     * Recupera tutte le prenotazioni relative a una specifica fiera.
     */
    List<PrenotazioneFieraDto> getPrenotazioniByFiera(Long idFiera);

    /**
     * Trova una prenotazione per id.
     */
    Optional<PrenotazioneFieraEntity> findById(Long id);

    /**
     * Elimina una prenotazione (solo dall’acquirente che l’ha fatta).
     */
    double eliminaById(Long idPrenotazione, String usernameAcquirente);
}
