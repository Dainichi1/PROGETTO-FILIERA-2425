package unicam.filiera.controller;

import unicam.filiera.dao.*;
import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.model.*;
import unicam.filiera.service.PacchettoService;
import unicam.filiera.service.PacchettoServiceImpl;
import unicam.filiera.service.ProdottoService;
import unicam.filiera.service.ProdottoServiceImpl;
import unicam.filiera.util.ValidatorePrenotazioneVisita;
import unicam.filiera.view.PannelloDistributore;
import unicam.filiera.view.PannelloProduttore;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class DistributoreController {

    private final String username;
    private final PacchettoService pacchettoService;
    private final ProdottoService prodottoService;
    private final VisitaInvitoDAO visitaDAO = JdbcVisitaInvitoDAO.getInstance();
    private final PrenotazioneVisitaDAO prenotazioneVisitaDAO = JdbcPrenotazioneVisitaDAO.getInstance();

    public DistributoreController(String username,
                                  PacchettoService pacchettoService,
                                  ProdottoService prodottoService) {
        this.username = username;
        this.pacchettoService = pacchettoService;
        this.prodottoService = prodottoService;
    }

    public DistributoreController(String username) {
        this(username,
                new PacchettoServiceImpl(),
                new ProdottoServiceImpl(JdbcProdottoDAO.getInstance()));
    }


    // ———————————————— metodi di sola lettura ————————————————

    /**
     * Prodotti APPROVATI (visibili in marketplace).
     */
    public List<Prodotto> getProdottiMarketplace() {
        return prodottoService.getProdottiByStato(StatoProdotto.APPROVATO);
    }

    /**
     * Tutti i pacchetti creati da questo utente, di qualunque stato.
     */
    public List<Pacchetto> getPacchettiCreatiDaMe() {
        return pacchettoService.getPacchettiCreatiDa(username);
    }


    // ———————————————— creazione & invio ————————————————

    public void inviaPacchetto(PacchettoDto dto,
                               BiConsumer<Boolean, String> callback) {
        try {
            pacchettoService.creaPacchetto(dto, username);
            callback.accept(true, "Pacchetto inviato al curatore per approvazione!");
        } catch (IllegalArgumentException iae) {
            callback.accept(false, iae.getMessage());
        } catch (Exception ex) {
            callback.accept(false, "Errore inaspettato: " + ex.getMessage());
        }
    }

    public void gestisciInvioPacchetto(Map<String, String> datiInput,
                                       List<String> nomiProdotti,
                                       List<File> certificati,
                                       List<File> foto,
                                       BiConsumer<Boolean, String> callback) {
        try {
            PacchettoDto dto = new PacchettoDto(
                    datiInput.getOrDefault("nome", "").trim(),
                    datiInput.getOrDefault("descrizione", "").trim(),
                    datiInput.getOrDefault("indirizzo", "").trim(),
                    datiInput.getOrDefault("prezzo", "").trim(),
                    datiInput.getOrDefault("quantita", "").trim(),
                    nomiProdotti,
                    certificati,
                    foto
            );
            inviaPacchetto(dto, callback);
        } catch (Exception ex) {
            callback.accept(false, "Errore durante la preparazione del pacchetto: " + ex.getMessage());
        }
    }

    public boolean eliminaPacchetto(String nome) {
        try {
            pacchettoService.eliminaPacchetto(nome, username);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }


    // ——————————— modifica & rinvio pacchetto rifiutato ———————————

    public void gestisciModificaPacchetto(String nomeOriginale,
                                          Map<String, String> datiInput,
                                          List<String> nomiProdotti,
                                          List<File> certificati,
                                          List<File> foto,
                                          BiConsumer<Boolean, String> callback) {
        try {
            PacchettoDto dto = new PacchettoDto(
                    nomeOriginale,
                    datiInput.getOrDefault("nome", "").trim(),
                    datiInput.getOrDefault("descrizione", "").trim(),
                    datiInput.getOrDefault("indirizzo", "").trim(),
                    datiInput.getOrDefault("prezzo", "").trim(),
                    datiInput.getOrDefault("quantita", "").trim(),
                    nomiProdotti,
                    certificati,
                    foto
            );
            pacchettoService.aggiornaPacchetto(nomeOriginale, dto, username);
            callback.accept(true, "Pacchetto aggiornato e rinviato al curatore!");
        } catch (IllegalArgumentException iae) {
            callback.accept(false, iae.getMessage());
        } catch (Exception ex) {
            callback.accept(false, "Errore inaspettato durante l'aggiornamento: " + ex.getMessage());
        }
    }

    public Pacchetto trovaPacchettoPerNome(String nome) {
        return pacchettoService.getPacchettiCreatiDa(username).stream()
                .filter(p -> p.getNome().equals(nome))
                .findFirst()
                .orElse(null);
    }

    public void visualizzaVisiteDisponibili(PannelloDistributore view) {
        List<VisitaInvito> visite = visitaDAO.findByDestinatario(username);
        view.showVisiteDisponibili(visite);
    }

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

    public void visualizzaPrenotazioniVisite(PannelloDistributore view) {
        List<PrenotazioneVisita> prenotazioni = prenotazioneVisitaDAO.findByUsername(username);
        List<VisitaInvito> tutteLeVisite = visitaDAO.findAll(); // opzionale, se vuoi mostrare descrizioni etc.
        view.showPrenotazioniVisite(prenotazioni, tutteLeVisite);
    }

    public void eliminaPrenotazioneVisita(long idPrenotazione, BiConsumer<String, Boolean> callback) {
        PrenotazioneVisita pren = prenotazioneVisitaDAO.findById(idPrenotazione);
        if (pren == null) {
            callback.accept("Prenotazione non trovata.", false);
            return;
        }
        // Puoi aggiungere eventuali controlli di autorizzazione (es: username == pren.getUsernameVenditore())

        boolean deleted = prenotazioneVisitaDAO.delete(idPrenotazione);
        if (deleted) {
            // (Opzionale) Aggiornamento del numero partecipanti nella tabella delle visite, se gestisci questo campo.
            callback.accept("Prenotazione eliminata con successo.", true);
        } else {
            callback.accept("Errore durante l'eliminazione della prenotazione.", false);
        }
    }

}
