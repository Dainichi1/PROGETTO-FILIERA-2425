package unicam.filiera.service;

import unicam.filiera.dto.FieraDto;
import unicam.filiera.model.Fiera;
import unicam.filiera.model.StatoEvento;

import java.util.List;

/**
 * Service per la gestione della logica di creazione e recupero di Fiere/Eventi.
 */
public interface FieraService {
    /**
     * Crea una nuova fiera/evento: validazione → mapping → persistenza → notifica.
     *
     * @param dto           dati dal form
     * @param organizzatore username di chi organizza
     * @throws IllegalArgumentException in caso di validazione fallita
     */
    void creaFiera(FieraDto dto, String organizzatore);

    /** Recupera le fiere create da un dato organizzatore. */
    List<Fiera> getFiereCreateDa(String organizzatore);

    /** Recupera le fiere filtrate per stato. */
    List<Fiera> getFiereByStato(StatoEvento stato);
}
