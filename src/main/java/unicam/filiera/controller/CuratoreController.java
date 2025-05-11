package unicam.filiera.controller;

import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.dao.PacchettoDAO;
import unicam.filiera.dao.JdbcProdottoDAO;
import unicam.filiera.dao.JdbcPacchettoDAO;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.observer.ProdottoNotifier;
import unicam.filiera.model.observer.PacchettoNotifier;

import java.util.List;

/**
 * Controller del Curatore per approvazione prodotti e pacchetti.
 * Usa iniezione di DAO e aggiorna lo stato attraverso l'interfaccia generica.
 */
public class CuratoreController {

    private final ProdottoDAO prodottoDAO;
    private final PacchettoDAO pacchettoDAO;
    private final ProdottoNotifier prodottoNotifier;
    private final PacchettoNotifier pacchettoNotifier;

    /**
     * Iniezione delle dipendenze (utile per test)
     */
    public CuratoreController(ProdottoDAO prodottoDAO, PacchettoDAO pacchettoDAO) {
        this.prodottoDAO = prodottoDAO;
        this.pacchettoDAO = pacchettoDAO;
        this.prodottoNotifier = ProdottoNotifier.getInstance();
        this.pacchettoNotifier = PacchettoNotifier.getInstance();
    }

    /**
     * Costruttore di convenienza per l'app reale
     */
    public CuratoreController() {
        this(JdbcProdottoDAO.getInstance(), JdbcPacchettoDAO.getInstance());
    }

    /**
     * Prodotti in attesa di approvazione
     */
    public List<Prodotto> getProdottiDaApprovare() {
        return prodottoDAO.findByStato(StatoProdotto.IN_ATTESA);
    }

    /**
     * Pacchetti in attesa di approvazione
     */
    public List<Pacchetto> getPacchettiDaApprovare() {
        return pacchettoDAO.findByStato(StatoProdotto.IN_ATTESA);
    }

    /**
     * Approva un prodotto e notifica gli osservatori
     */
    public boolean approvaProdotto(Prodotto prodotto) {
        prodotto.setStato(StatoProdotto.APPROVATO);
        prodotto.setCommento(null);
        boolean success = prodottoDAO.update(prodotto);
        if (success) {
            prodottoNotifier.notificaTutti(prodotto, "APPROVATO");
        }
        return success;
    }

    /**
     * Rifiuta un prodotto con commento e notifica
     */
    public boolean rifiutaProdotto(Prodotto prodotto, String commento) {
        prodotto.setStato(StatoProdotto.RIFIUTATO);
        prodotto.setCommento(commento != null ? commento : "");
        boolean success = prodottoDAO.update(prodotto);
        if (success) {
            prodottoNotifier.notificaTutti(prodotto, "RIFIUTATO");
        }
        return success;
    }

    /**
     * Approva un pacchetto e notifica gli osservatori
     */
    public boolean approvaPacchetto(Pacchetto pacchetto) {
        pacchetto.setStato(StatoProdotto.APPROVATO);
        pacchetto.setCommento(null);
        boolean success = pacchettoDAO.update(pacchetto);
        if (success) {
            pacchettoNotifier.notificaTutti(pacchetto, "APPROVATO");
        }
        return success;
    }

    /**
     * Rifiuta un pacchetto con commento e notifica
     */
    public boolean rifiutaPacchetto(Pacchetto pacchetto, String commento) {
        pacchetto.setStato(StatoProdotto.RIFIUTATO);
        pacchetto.setCommento(commento != null ? commento : "");
        boolean success = pacchettoDAO.update(pacchetto);
        if (success) {
            pacchettoNotifier.notificaTutti(pacchetto, "RIFIUTATO");
        }
        return success;
    }
}
