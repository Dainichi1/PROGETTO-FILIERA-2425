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
import unicam.filiera.util.ValidatoreAnnuncioEvento;

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

    public void pubblicaAnnuncioEvento(JComponent parent, AnnuncioEventoDto annuncio) {
        try {
            // validazioni
            ValidatoreAnnuncioEvento.validaCampiBase(annuncio);
            ValidatoreAnnuncioEvento.validaCoerenzaEvento(
                    annuncio,
                    organizzatore,
                    JdbcFieraDAO.getInstance(),
                    JdbcVisitaInvitoDAO.getInstance()
            );

            int choice = JOptionPane.showConfirmDialog(
                    parent,
                    "Sei sicuro di voler pubblicare l’annuncio sul Social?",
                    "Conferma pubblicazione",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice != JOptionPane.YES_OPTION) {
                SwingUtilities.getWindowAncestor(parent).dispose();
                return;
            }

            // mapping su PostSocialDto (idAcquisto lasciato NULL!)
            PostSocialDto post = new PostSocialDto();
            post.setAutoreUsername(organizzatore);
            post.setTitolo(annuncio.getTitolo());
            post.setTesto(annuncio.getTesto());
            post.setTipoItem("Evento");
            String nome = ("FIERA".equalsIgnoreCase(annuncio.getTipoEvento()) ? "Fiera" : "Visita")
                    + " #" + annuncio.getEventoId();
            post.setNomeItem(nome);

            try (var conn = DatabaseManager.getConnection()) {
                new JdbcSocialPostDAO(conn).pubblicaPost(post);
            }

            JOptionPane.showMessageDialog(parent, "Annuncio pubblicato con successo!",
                    "Successo", JOptionPane.INFORMATION_MESSAGE);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(),
                    "Errore di validazione", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException | java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(parent, "Errore durante la pubblicazione dell’annuncio",
                    "Errore", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public List<VisitaInvito> getVisiteInvitoCreateDaMe() {
        return visitaService.getVisiteCreateDa(organizzatore);
    }

    public List<UtenteAutenticato> getUtentiPerRuoli(Ruolo... ruoli) {
        return utenteDAO.findByRuoli(List.of(ruoli));
    }
}
