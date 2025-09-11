package unicam.filiera.controller;

import unicam.filiera.dao.*;
import unicam.filiera.dto.AnnuncioEventoDto;
import unicam.filiera.dto.FieraDto;
import unicam.filiera.dto.PostSocialDto;
import unicam.filiera.dto.VisitaInvitoDto;
import unicam.filiera.model.*;
import unicam.filiera.service.FieraService;
import unicam.filiera.service.FieraServiceImpl;
import unicam.filiera.service.VisitaInvitoService;
import unicam.filiera.service.VisitaInvitoServiceImpl;


import javax.swing.*;
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

    public List<Fiera> getFierePubblicateDaMe() {
        return fieraService.getFiereCreateDa(organizzatore).stream()
                .filter(f -> f.getStato() == StatoEvento.PUBBLICATA)
                .toList();
    }

    public List<VisitaInvito> getVisitePubblicateDaMe() {
        return visitaService.getVisiteCreateDa(organizzatore).stream()
                .filter(v -> v.getStato() == StatoEvento.PUBBLICATA)
                .toList();
    }

    /**
     * Ritorna tutti i post pubblicati sul social, ordinati per data desc.
     */
    public List<PostSocialDto> getSocialFeed() {
        try (var conn = DatabaseManager.getConnection()) {
            var dao = new JdbcSocialPostDAO(conn);
            return dao.findAllOrderByDataDesc();
        } catch (Exception ex) {
            throw new RuntimeException("Errore nel caricamento del social network", ex);
        }
    }



    public List<VisitaInvito> getVisiteInvitoCreateDaMe() {
        return visitaService.getVisiteCreateDa(organizzatore);
    }

    public List<UtenteAutenticato> getUtentiPerRuoli(Ruolo... ruoli) {
        return utenteDAO.findByRuoli(List.of(ruoli));
    }
}
