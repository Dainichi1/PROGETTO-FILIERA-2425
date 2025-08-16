package unicam.filiera.controller;

import unicam.filiera.dao.*;
import unicam.filiera.dto.PostSocialDto;
import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.model.PrenotazioneVisita;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.VisitaInvito;
import unicam.filiera.service.ProdottoService;
import unicam.filiera.service.ProdottoServiceImpl;
import unicam.filiera.util.ValidatoreAnnuncioItem;
import unicam.filiera.util.ValidatorePrenotazioneVisita;
import unicam.filiera.view.PannelloProduttore;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ProduttoreController {

    private final String username;
    private final ProdottoService service;
    private final VisitaInvitoDAO visitaDAO = JdbcVisitaInvitoDAO.getInstance();
    private final PrenotazioneVisitaDAO prenotazioneVisitaDAO = JdbcPrenotazioneVisitaDAO.getInstance();

    public ProduttoreController(String username, ProdottoService service) {
        this.username = username;
        this.service = service;
    }

    public ProduttoreController(String username) {
        this(username, new ProdottoServiceImpl(JdbcProdottoDAO.getInstance()));
    }

    // ——————————————— metodi di sola lettura ———————————————

    /**
     * Restituisce tutti i prodotti creati da questo utente.
     */
    public List<Prodotto> getProdottiCreatiDaMe() {
        return service.getProdottiCreatiDa(username);
    }

    /**
     * Recupera un singolo prodotto per nome (usato in UI per la modifica).
     */
    public Prodotto trovaProdottoPerNome(String nome) {
        return getProdottiCreatiDaMe().stream()
                .filter(p -> p.getNome().equals(nome))
                .findFirst()
                .orElse(null);
    }

    // ——————————————— creazione & invio ———————————————

    public void inviaProdotto(ProdottoDto dto, BiConsumer<Boolean, String> callback) {
        try {
            service.creaProdotto(dto, username);
            callback.accept(true, "Prodotto inviato al Curatore!");
        } catch (IllegalArgumentException iae) {
            callback.accept(false, iae.getMessage());
        } catch (Exception ex) {
            callback.accept(false, "Errore inaspettato: " + ex.getMessage());
        }
    }

    public void gestisciInvioProdotto(Map<String, String> datiInput,
                                      List<File> certificati,
                                      List<File> foto,
                                      BiConsumer<Boolean, String> callback) {
        try {
            ProdottoDto dto = new ProdottoDto(
                    datiInput.getOrDefault("nome", ""),
                    datiInput.getOrDefault("descrizione", ""),
                    datiInput.getOrDefault("quantita", ""),
                    datiInput.getOrDefault("prezzo", ""),
                    datiInput.getOrDefault("indirizzo", ""),
                    certificati,
                    foto
            );
            inviaProdotto(dto, callback);
        } catch (Exception ex) {
            callback.accept(false, "Errore durante la preparazione del prodotto: " + ex.getMessage());
        }
    }

    // ——————————————— elimina ———————————————

    public boolean eliminaProdotto(String nome) {
        try {
            service.eliminaProdotto(nome, username);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    // ————————— modifica & rinvio prodotto rifiutato ————————

    public void gestisciModificaProdotto(String originalName,
                                         Map<String, String> datiInput,
                                         List<File> certificati,
                                         List<File> foto,
                                         BiConsumer<Boolean, String> callback) {
        try {
            ProdottoDto dto = new ProdottoDto(
                    datiInput.getOrDefault("nome", ""),
                    datiInput.getOrDefault("descrizione", ""),
                    datiInput.getOrDefault("quantita", ""),
                    datiInput.getOrDefault("prezzo", ""),
                    datiInput.getOrDefault("indirizzo", ""),
                    certificati,
                    foto
            );
            service.aggiornaProdotto(originalName, dto, username);
            callback.accept(true, "Prodotto aggiornato e rinviato al Curatore!");
        } catch (IllegalArgumentException iae) {
            callback.accept(false, iae.getMessage());
        } catch (Exception ex) {
            callback.accept(false, "Errore inaspettato durante l'aggiornamento: " + ex.getMessage());
        }
    }

    public void visualizzaVisiteDisponibili(PannelloProduttore view) {
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

    public void visualizzaPrenotazioniVisite(PannelloProduttore view) {
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

    public void pubblicaAnnuncioItem(String nomeProdotto,
                                     String titolo,
                                     String testo,
                                     java.util.function.BiConsumer<String, Boolean> callback) {
        try {
            // 1) Validazione
            ValidatoreAnnuncioItem.valida(
                    titolo, testo, nomeProdotto, username, JdbcProdottoDAO.getInstance()
            );

            // 2) Mapping su PostSocialDto (id_acquisto = NULL)
            PostSocialDto post = new PostSocialDto();
            post.setAutoreUsername(username);
            post.setNomeItem(nomeProdotto);
            post.setTipoItem("Prodotto");
            post.setTitolo(titolo);
            post.setTesto(testo);

            // 3) Persistenza
            try (var conn = DatabaseManager.getConnection()) {
                new JdbcSocialPostDAO(conn).pubblicaPost(post);
            }

            // 4) Esito OK
            callback.accept("Annuncio pubblicato con successo!", true);

        } catch (IllegalArgumentException ex) {
            callback.accept(ex.getMessage(), false);
        } catch (RuntimeException | java.sql.SQLException ex) {
            ex.printStackTrace();
            callback.accept("Errore durante la pubblicazione dell'annuncio.", false);
        }
    }

    public List<PostSocialDto> getSocialFeed() {
        try (var conn = DatabaseManager.getConnection()) {
            var dao = new JdbcSocialPostDAO(conn);
            return dao.findAllOrderByDataDesc();
        } catch (Exception ex) {
            throw new RuntimeException("Errore nel caricamento del social network", ex);
        }
    }


}
