package unicam.filiera.controller;

import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.service.ProdottoService;
import unicam.filiera.service.ProdottoServiceImpl;
import unicam.filiera.dao.JdbcProdottoDAO;

import java.util.List;

public class ProduttoreController {
    @FunctionalInterface
    public interface EsitoListener {
        void completato(Boolean ok, String msg);
    }

    private final ProdottoService service;
    private final String username;

    /** Iniezione del service*/
    public ProduttoreController(ProdottoService service, String username) {
        this.service = service;
        this.username = username;
    }

    /** Costruttore di convenienza per l’app reale */
    public ProduttoreController(String username) {
        this(new ProdottoServiceImpl(JdbcProdottoDAO.getInstance()), username);
    }

    /**
     * Invia il DTO al service; in caso di validazione fallita,
     * il ValidatoreProdotto lancerà IllegalArgumentException.
     */
    public void inviaProdotto(ProdottoDto dto, EsitoListener callback) {
        try {
            service.creaProdotto(dto, username);
            callback.completato(true, "Prodotto inviato al Curatore!");
        } catch (IllegalArgumentException iae) {
            // qui catturiamo i messaggi dei nostri validatori
            callback.completato(false, iae.getMessage());
        } catch (Exception e) {
            callback.completato(false, "Errore: " + e.getMessage());
        }
    }

    /** Restituisce i prodotti creati da questo utente */
    public List<Prodotto> getProdottiCreatiDaMe() {
        return service.getProdottiCreatiDa(username);
    }
}
