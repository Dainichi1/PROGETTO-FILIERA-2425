package unicam.filiera.service;

import unicam.filiera.dto.PrenotazioneVisitaDto;
import unicam.filiera.entity.PrenotazioneVisitaEntity;

import java.util.List;
import java.util.Optional;

public interface PrenotazioneVisitaService {

    /**
     * Crea una nuova prenotazione per una visita guidata.
     *
     * @param dto DTO contenente idVisita e numeroPersone
     * @param usernameVenditore username autenticato del venditore
     */
    void creaPrenotazione(PrenotazioneVisitaDto dto, String usernameVenditore);

    /**
     * Recupera tutte le prenotazioni fatte da un venditore.
     */
    List<PrenotazioneVisitaDto> getPrenotazioniByVenditore(String usernameVenditore);

    /**
     * Recupera tutte le prenotazioni relative a una specifica visita.
     */
    List<PrenotazioneVisitaDto> getPrenotazioniByVisita(Long idVisita);

    /**
     * Trova una prenotazione per id.
     */
    Optional<PrenotazioneVisitaEntity> findById(Long id);

    /**
     * Elimina una prenotazione (solo dal venditore che l’ha fatta).
     */
    void eliminaById(Long idPrenotazione, String usernameVenditore);
}
