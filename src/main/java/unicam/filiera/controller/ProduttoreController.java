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
    @FunctionalInterface
    public interface EsitoListener {
        void completato(Boolean ok, String msg);
    }

    private final ProdottoService service;
    private final String username;

    /**
     * Iniezione del service
     */
    public ProduttoreController(ProdottoService service, String username) {
        this.service = service;
        this.username = username;
    }

    /**
     * Costruttore di convenienza per l’app reale
     */
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

    /**
     * Restituisce i prodotti creati da questo utente
     */
    public List<Prodotto> getProdottiCreatiDaMe() {
        return service.getProdottiCreatiDa(username);
    }


    public void gestisciInvioProdotto(Map<String, String> datiInput,
                                      List<File> certificati,
                                      List<File> foto,
                                      BiConsumer<String, Boolean> callback) {
        try {
            ProdottoDto dto = new ProdottoDto(
                    datiInput.getOrDefault("nome", "").trim(),
                    datiInput.getOrDefault("descrizione", "").trim(),
                    datiInput.getOrDefault("quantita", "").trim(),
                    datiInput.getOrDefault("prezzo", "").trim(),
                    datiInput.getOrDefault("indirizzo", "").trim(),
                    List.copyOf(certificati),
                    List.copyOf(foto)
            );
            this.inviaProdotto(dto, (ok, msg) -> callback.accept(msg, ok));
        } catch (Exception ex) {
            callback.accept("Errore durante l'invio: " + ex.getMessage(), false);
        }
    }

}
