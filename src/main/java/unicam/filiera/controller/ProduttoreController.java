package unicam.filiera.controller;

import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.service.ProdottoService;
import unicam.filiera.service.ProdottoServiceImpl;
import unicam.filiera.dao.JdbcProdottoDAO;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ProduttoreController {

    private final String username;
    private final ProdottoService service;

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
}
