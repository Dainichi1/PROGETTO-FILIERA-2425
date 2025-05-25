package unicam.filiera.controller;

import unicam.filiera.dao.*;
import unicam.filiera.model.*;
import unicam.filiera.util.ValidatorePrenotazioneVisita;
import unicam.filiera.view.PannelloTrasformatore;

import java.util.List;
import java.util.function.BiConsumer;

public class TrasformatoreController {

    private final String username;
    private final VisitaInvitoDAO visitaDAO = JdbcVisitaInvitoDAO.getInstance();
    private final PrenotazioneVisitaDAO prenotazioneVisitaDAO = JdbcPrenotazioneVisitaDAO.getInstance();

    public TrasformatoreController(String username) {
        this.username = username;
    }

    // Visualizza tutte le visite disponibili per il trasformatore
    public void visualizzaVisiteDisponibili(PannelloTrasformatore view) {
        List<VisitaInvito> visite = visitaDAO.findByDestinatario(username);
        view.showVisiteDisponibili(visite);
    }

    // Prenota una visita
    public void prenotaVisita(long idVisita, int numeroPersone, BiConsumer<String, Boolean> callback) {
        VisitaInvito visita = visitaDAO.findById(idVisita);

        try {
            ValidatorePrenotazioneVisita.validaPrenotazione(
                    idVisita, numeroPersone, visita, username, prenotazioneVisitaDAO
            );

            PrenotazioneVisita pren = new PrenotazioneVisita(
                    idVisita, username, numeroPersone, java.time.LocalDateTime.now()
            );
            boolean ok = prenotazioneVisitaDAO.save(pren);

            if (ok) {
                callback.accept("Prenotazione effettuata con successo!", true);
            } else {
                callback.accept("Errore durante la prenotazione.", false);
            }
        } catch (IllegalArgumentException ex) {
            callback.accept(ex.getMessage(), false);
        }
    }

    // Visualizza tutte le prenotazioni effettuate
    public void visualizzaPrenotazioniVisite(PannelloTrasformatore view) {
        List<PrenotazioneVisita> prenotazioni = prenotazioneVisitaDAO.findByUsername(username);
        List<VisitaInvito> tutteLeVisite = visitaDAO.findAll(); // opzionale, se vuoi mostrare descrizioni etc.
        view.showPrenotazioniVisite(prenotazioni, tutteLeVisite);
    }

    // Elimina una prenotazione
    public void eliminaPrenotazioneVisita(long idPrenotazione, BiConsumer<String, Boolean> callback) {
        PrenotazioneVisita pren = prenotazioneVisitaDAO.findById(idPrenotazione);
        if (pren == null) {
            callback.accept("Prenotazione non trovata.", false);
            return;
        }
        // (eventuale controllo autorizzazione)

        boolean deleted = prenotazioneVisitaDAO.delete(idPrenotazione);
        if (deleted) {
            callback.accept("Prenotazione eliminata con successo.", true);
        } else {
            callback.accept("Errore durante l'eliminazione della prenotazione.", false);
        }
    }

}
