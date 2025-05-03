// -------- VisitaInvitoService.java --------
package unicam.filiera.service;

import unicam.filiera.dto.VisitaInvitoDto;
import unicam.filiera.model.VisitaInvito;

import java.util.List;

/**
 * Service per la gestione della logica di creazione e recupero
 * di visite su invito.
 */
public interface VisitaInvitoService {
    /**
     * Crea una nuova visita su invito: validazione → mapping →
     * persistenza → notifica.
     *
     * @param dto           dati dal form
     * @param organizzatore username di chi organizza
     * @throws IllegalArgumentException in caso di validazione fallita
     */
    void creaVisitaInvito(VisitaInvitoDto dto, String organizzatore);

    /** Recupera le visite create da un dato organizzatore. */
    List<VisitaInvito> getVisiteCreateDa(String organizzatore);
}
