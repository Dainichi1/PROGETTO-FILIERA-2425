package unicam.filiera.controller;

import unicam.filiera.dao.*;
import unicam.filiera.dto.PostSocialDto;
import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.model.*;
import unicam.filiera.service.ProdottoTrasformatoService;
import unicam.filiera.service.ProdottoTrasformatoServiceImpl;
import unicam.filiera.util.ValidatoreAnnuncioItem;
import unicam.filiera.util.ValidatorePrenotazioneVisita;
import unicam.filiera.view.PannelloTrasformatore; // o la tua view

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Controller per la logica di gestione prodotti trasformati e visite lato Trasformatore.
 */
public class TrasformatoreController {

    private final String username;
    private final ProdottoTrasformatoService service;
    private final VisitaInvitoDAO visitaDAO = JdbcVisitaInvitoDAO.getInstance();
    private final PrenotazioneVisitaDAO prenotazioneVisitaDAO = JdbcPrenotazioneVisitaDAO.getInstance();
    private final ProduttoreDAO produttoreDAO = JdbcProduttoreDAO.getInstance();
    private final ProdottoDAO prodottoDAO = JdbcProdottoDAO.getInstance();


    // Costruttori
    public TrasformatoreController(String username, ProdottoTrasformatoService service) {
        this.username = username;
        this.service = service;
    }

    public TrasformatoreController(String username) {
        this(username, new ProdottoTrasformatoServiceImpl(JdbcProdottoTrasformatoDAO.getInstance()));
    }

    // ———————————— SEZIONE PRODOTTI TRASFORMATI ————————————

    /**
     * Restituisce tutti i prodotti trasformati creati da questo utente.
     */
    public List<ProdottoTrasformato> getProdottiTrasformatiCreatiDaMe() {
        return service.getProdottiTrasformatiCreatiDa(username);
    }

    /**
     * Recupera un singolo prodotto trasformato per nome (per la modifica in UI).
     */
    public ProdottoTrasformato trovaProdottoTrasformatoPerNome(String nome) {
        return getProdottiTrasformatiCreatiDaMe().stream()
                .filter(p -> p.getNome().equals(nome))
                .findFirst()
                .orElse(null);
    }

    /**
     * Invia un nuovo prodotto trasformato al Curatore.
     */
    public void inviaProdottoTrasformato(ProdottoTrasformatoDto dto, BiConsumer<Boolean, String> callback) {
        try {
            service.creaProdottoTrasformato(dto, username);
            callback.accept(true, "Prodotto trasformato inviato al Curatore!");
        } catch (IllegalArgumentException iae) {
            callback.accept(false, iae.getMessage());
        } catch (Exception ex) {
            callback.accept(false, "Errore inaspettato: " + ex.getMessage());
        }
    }

    /**
     * Gestisce la preparazione e invio del prodotto trasformato (raccolta dati UI → dto).
     */
    public void gestisciInvioProdottoTrasformato(Map<String, String> datiInput,
                                                 List<File> certificati,
                                                 List<File> foto,
                                                 List<ProdottoTrasformatoDto.FaseProduzioneDto> fasi,
                                                 BiConsumer<Boolean, String> callback) {
        try {
            ProdottoTrasformatoDto dto = new ProdottoTrasformatoDto(
                    datiInput.getOrDefault("nome", ""),
                    datiInput.getOrDefault("descrizione", ""),
                    datiInput.getOrDefault("quantita", ""),
                    datiInput.getOrDefault("prezzo", ""),
                    datiInput.getOrDefault("indirizzo", ""),
                    certificati,
                    foto,
                    fasi
            );
            inviaProdottoTrasformato(dto, callback);
        } catch (Exception ex) {
            callback.accept(false, "Errore durante la preparazione del prodotto trasformato: " + ex.getMessage());
        }
    }

    /**
     * Elimina un prodotto trasformato.
     */
    public boolean eliminaProdottoTrasformato(String nome) {
        try {
            service.eliminaProdottoTrasformato(nome, username);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    /**
     * Gestisce la modifica e rinvio di un prodotto trasformato rifiutato.
     */
    public void gestisciModificaProdottoTrasformato(String originalName,
                                                    Map<String, String> datiInput,
                                                    List<File> certificati,
                                                    List<File> foto,
                                                    List<ProdottoTrasformatoDto.FaseProduzioneDto> fasi,
                                                    BiConsumer<Boolean, String> callback) {
        try {
            ProdottoTrasformatoDto dto = new ProdottoTrasformatoDto(
                    originalName,
                    datiInput.getOrDefault("nome", ""),
                    datiInput.getOrDefault("descrizione", ""),
                    datiInput.getOrDefault("quantita", ""),
                    datiInput.getOrDefault("prezzo", ""),
                    datiInput.getOrDefault("indirizzo", ""),
                    certificati,
                    foto,
                    fasi
            );
            service.aggiornaProdottoTrasformato(originalName, dto, username);
            callback.accept(true, "Prodotto trasformato aggiornato e rinviato al Curatore!");
        } catch (IllegalArgumentException iae) {
            callback.accept(false, iae.getMessage());
        } catch (Exception ex) {
            callback.accept(false, "Errore inaspettato durante l'aggiornamento: " + ex.getMessage());
        }
    }

    // ———————————— SEZIONE VISITE INVITO ————————————

    /**
     * Visualizza le visite disponibili per questo trasformatore (ad es. nella UI).
     */
    public void visualizzaVisiteDisponibili(PannelloTrasformatore view) {
        List<VisitaInvito> visite = visitaDAO.findByDestinatario(username);
        view.showVisiteDisponibili(visite);
    }

    /**
     * Prenota una visita invito.
     */
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

    /**
     * Visualizza tutte le prenotazioni di visite effettuate dal trasformatore.
     */
    public void visualizzaPrenotazioniVisite(PannelloTrasformatore view) {
        List<PrenotazioneVisita> prenotazioni = prenotazioneVisitaDAO.findByUsername(username);
        List<VisitaInvito> tutteLeVisite = visitaDAO.findAll(); // opzionale, per arricchire le info
        view.showPrenotazioniVisite(prenotazioni, tutteLeVisite);
    }

    /**
     * Elimina una prenotazione di visita.
     */
    public void eliminaPrenotazioneVisita(long idPrenotazione, BiConsumer<String, Boolean> callback) {
        PrenotazioneVisita pren = prenotazioneVisitaDAO.findById(idPrenotazione);
        if (pren == null) {
            callback.accept("Prenotazione non trovata.", false);
            return;
        }
        // Autorizzazione: (opzionale) puoi aggiungere controlli sull'utente
        boolean deleted = prenotazioneVisitaDAO.delete(idPrenotazione);
        if (deleted) {
            callback.accept("Prenotazione eliminata con successo.", true);
        } else {
            callback.accept("Errore durante l'eliminazione della prenotazione.", false);
        }
    }

    /**
     * Restituisce la lista dei produttori disponibili.
     */
    public List<Produttore> getProduttoriDisponibili() {
        return produttoreDAO.findAll(); // oppure altro metodo se vuoi filtri
    }

    public List<Prodotto> getProdottiApprovatiByProduttore(String usernameProduttore) {
        return prodottoDAO.findProdottiApprovatiByProduttore(usernameProduttore);
    }

    public void pubblicaAnnuncioProdottoTrasformato(
            String nomeProdotto,
            String titolo,
            String testo,
            java.util.function.BiConsumer<String, Boolean> callback) {
        try {
            // Validazione corretta sui prodotti trasformati
            ValidatoreAnnuncioItem.validaProdottoTrasformato(
                    titolo, testo, nomeProdotto, username, JdbcProdottoTrasformatoDAO.getInstance()
            );

            PostSocialDto post = new PostSocialDto();
            post.setAutoreUsername(username);
            post.setNomeItem(nomeProdotto);
            post.setTipoItem("ProdottoTrasformato"); // o "Prodotto trasformato", ma sii coerente ovunque
            post.setTitolo(titolo);
            post.setTesto(testo);

            try (var conn = DatabaseManager.getConnection()) {
                new JdbcSocialPostDAO(conn).pubblicaPost(post);
            }

            callback.accept("Annuncio pubblicato con successo!", true);
        } catch (IllegalArgumentException ex) {
            callback.accept(ex.getMessage(), false);
        } catch (RuntimeException | java.sql.SQLException ex) {
            ex.printStackTrace();
            callback.accept("Errore durante la pubblicazione dell'annuncio.", false);
        }
    }

    // in TrasformatoreController
    public List<PostSocialDto> getSocialFeed() {
        try (var conn = DatabaseManager.getConnection()) {
            JdbcSocialPostDAO dao = new JdbcSocialPostDAO(conn);
            return dao.findAllOrderByDataDesc(); // oppure dao.findAll() se non hai il metodo ordinato
        } catch (Exception ex) {
            throw new RuntimeException("Errore nel caricamento del social network", ex);
        }
    }


}
