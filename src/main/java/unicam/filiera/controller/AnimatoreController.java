package unicam.filiera.controller;

import unicam.filiera.dto.FieraDto;
import unicam.filiera.model.Fiera;
import unicam.filiera.service.FieraService;
import unicam.filiera.service.FieraServiceImpl;
import unicam.filiera.dao.JdbcFieraDAO;

import java.util.List;

public class AnimatoreController {
    @FunctionalInterface
    public interface EsitoListener {
        void completato(boolean ok, String msg);
    }

    private final FieraService service;
    private final String       organizzatore;

    /** Iniezione del service (per i test) */
    public AnimatoreController(FieraService service, String organizzatore) {
        this.service     = service;
        this.organizzatore = organizzatore;
    }

    /** Convenienza per l’app reale */
    public AnimatoreController(String organizzatore) {
        this(new FieraServiceImpl(JdbcFieraDAO.getInstance()), organizzatore);
    }

    /**
     * Invio del DTO al service; in caso di validazione fallita
     * lancerà IllegalArgumentException con il messaggio di errore.
     */
    public void inviaFiera(FieraDto dto, EsitoListener callback) {
        try {
            service.creaFiera(dto, organizzatore);
            callback.completato(true, "Fiera inviata per pubblicazione!");
        } catch (IllegalArgumentException iae) {
            callback.completato(false, iae.getMessage());
        } catch (Exception e) {
            callback.completato(false, "Errore inatteso: " + e.getMessage());
        }
    }

    /** Permette alla UI di ricaricare la lista delle fiere create da questo animatore */
    public List<Fiera> getFiereCreateDaMe() {
        return service.getFiereCreateDa(organizzatore);
    }
}
