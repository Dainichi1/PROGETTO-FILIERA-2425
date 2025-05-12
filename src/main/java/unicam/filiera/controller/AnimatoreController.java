package unicam.filiera.controller;

import unicam.filiera.dao.JdbcUtenteDAO;
import unicam.filiera.dao.UtenteDAO;
import unicam.filiera.dto.FieraDto;
import unicam.filiera.dto.VisitaInvitoDto;
import unicam.filiera.model.Fiera;
import unicam.filiera.model.Ruolo;
import unicam.filiera.model.UtenteAutenticato;
import unicam.filiera.model.VisitaInvito;
import unicam.filiera.service.FieraService;
import unicam.filiera.service.FieraServiceImpl;
import unicam.filiera.service.VisitaInvitoService;
import unicam.filiera.service.VisitaInvitoServiceImpl;
import unicam.filiera.dao.JdbcFieraDAO;

import java.util.List;

public class AnimatoreController {

    @FunctionalInterface
    public interface EsitoListener {
        void completato(boolean ok, String msg);
    }

    private final FieraService fieraService;
    private final VisitaInvitoService visitaService;
    private final String organizzatore;
    private final UtenteDAO utenteDAO;

    public AnimatoreController(FieraService fieraService,
                               VisitaInvitoService visitaService,
                               UtenteDAO utenteDAO,
                               String organizzatore) {
        this.fieraService = fieraService;
        this.visitaService = visitaService;
        this.utenteDAO = utenteDAO;
        this.organizzatore = organizzatore;
    }

    public AnimatoreController(String organizzatore) {
        this(
                new FieraServiceImpl(JdbcFieraDAO.getInstance()),
                new VisitaInvitoServiceImpl(),
                JdbcUtenteDAO.getInstance(),
                organizzatore
        );
    }

    public void inviaFiera(FieraDto dto, EsitoListener callback) {
        try {
            fieraService.creaFiera(dto, organizzatore);
            callback.completato(true, "Fiera inviata per pubblicazione!");
        } catch (IllegalArgumentException iae) {
            callback.completato(false, iae.getMessage());
        } catch (Exception e) {
            callback.completato(false, "Errore inatteso: " + e.getMessage());
        }
    }

    public List<Fiera> getFiereCreateDaMe() {
        return fieraService.getFiereCreateDa(organizzatore);
    }

    public void inviaVisitaInvito(VisitaInvitoDto dto, EsitoListener callback) {
        try {
            visitaService.creaVisitaInvito(dto, organizzatore);
            callback.completato(true, "Visita su invito pubblicata!");
        } catch (IllegalArgumentException iae) {
            callback.completato(false, iae.getMessage());
        } catch (Exception e) {
            callback.completato(false, "Errore inatteso: " + e.getMessage());
        }
    }

    public List<VisitaInvito> getVisiteInvitoCreateDaMe() {
        return visitaService.getVisiteCreateDa(organizzatore);
    }

    public List<UtenteAutenticato> getUtentiPerRuoli(Ruolo... ruoli) {
        return utenteDAO.findByRuoli(List.of(ruoli));
    }
}
