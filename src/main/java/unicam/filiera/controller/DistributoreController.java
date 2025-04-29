package unicam.filiera.controller;

import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.service.PacchettoService;
import unicam.filiera.service.PacchettoServiceImpl;
import unicam.filiera.service.ProdottoService;
import unicam.filiera.service.ProdottoServiceImpl;
import unicam.filiera.dao.JdbcProdottoDAO;

import java.io.File;
import java.util.List;
import java.util.function.BiConsumer;

public class DistributoreController {

    private final String          username;
    private final PacchettoService pacchettoService;
    private final ProdottoService  prodottoService;

    /** iniezione di dipendenza per i test */
    public DistributoreController(String username,
                                  PacchettoService pacchettoService,
                                  ProdottoService prodottoService) {
        this.username         = username;
        this.pacchettoService = pacchettoService;
        this.prodottoService  = prodottoService;
    }

    /** costruttore di convenienza per l’app reale */
    public DistributoreController(String username) {
        this(username,
                new PacchettoServiceImpl(),
                new ProdottoServiceImpl(JdbcProdottoDAO.getInstance()));
    }

    /* ======================================================================
       SERVIZI DI SOLA-LETTURA (usati dalla view)
       ====================================================================== */
    /** Prodotti approvati e quindi visibili nel Marketplace. */
    public List<Prodotto> getProdottiMarketplace() {
        return prodottoService.getProdottiByStato(StatoProdotto.APPROVATO);
    }

    /** Pacchetti creati dallo stesso distributore. */
    public List<Pacchetto> getPacchettiCreatiDaMe() {
        return pacchettoService.getPacchettiCreatiDa(username);
    }

    /* ======================================================================
       CASO D’USO “CREA & INVIA PACCHETTO”
       ====================================================================== */
    /**
     * Flusso completo: validazione, persistenza, upload file,
     * passaggio in stato IN_ATTESA e notifica al Curatore.
     * Il risultato viene ritornato tramite callback per non far dipendere
     * la view dalla logica.
     */
    public void inviaPacchetto(PacchettoDto dto,
                               BiConsumer<Boolean,String> callback) {
        try {
            // delego tutto al service
            pacchettoService.creaPacchetto(dto, username);
            callback.accept(true, "Pacchetto inviato al curatore per approvazione!");
        } catch (IllegalArgumentException iae) {
            // validazioni fallite
            callback.accept(false, iae.getMessage());
        } catch (Exception ex) {
            // ogni altro errore
            callback.accept(false, "Errore inaspettato: " + ex.getMessage());
        }
    }
}
