package unicam.filiera.controller;

import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.observer.ProdottoNotifier;

import java.util.List;

public class CuratoreController {

    private final ProdottoDAO prodottoDAO;

    public CuratoreController() {
        this.prodottoDAO = new ProdottoDAO();
    }

    /**
     * Restituisce tutti i prodotti che hanno stato "IN_ATTESA".
     */
    public List<Prodotto> getProdottiDaApprovare() {
        return prodottoDAO.getProdottiByStato(StatoProdotto.IN_ATTESA);
    }

    /**
     * Approva un prodotto, aggiornandone lo stato nel DB e notificando l'evento.
     */
    public boolean approvaProdotto(Prodotto prodotto) {
        boolean success = prodottoDAO.aggiornaStatoProdotto(prodotto, StatoProdotto.APPROVATO);
        if (success) {
            ProdottoNotifier.getInstance().notificaTutti(prodotto, "APPROVATO");
        }
        return success;
    }

    /**
     * Rifiuta un prodotto, aggiornando stato e commento, e notificando l'evento.
     */
    public boolean rifiutaProdotto(Prodotto prodotto, String commento) {
        if (commento == null) commento = "";

        boolean success = prodottoDAO.aggiornaStatoECommentoProdotto(prodotto, StatoProdotto.RIFIUTATO, commento);
        if (success) {
            ProdottoNotifier.getInstance().notificaTutti(prodotto, "RIFIUTATO");
        }
        return success;
    }
}
