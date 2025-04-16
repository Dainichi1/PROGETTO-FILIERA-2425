package unicam.filiera.controller;

import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;

import java.util.List;

public class CuratoreController {

    private final ProdottoDAO prodottoDAO;

    public CuratoreController() {
        this.prodottoDAO = new ProdottoDAO();
    }

    /**
     * Restituisce tutti i prodotti che hanno stato "IN_ATTESA".
     */
    public List<Prodotto> getProdottiInAttesa() {
        return prodottoDAO.getProdottiByStato(StatoProdotto.IN_ATTESA);
    }

    /**
     * Approva un prodotto, aggiornandone lo stato nel DB.
     */
    public boolean approvaProdotto(Prodotto prodotto) {
        return prodottoDAO.aggiornaStatoProdotto(prodotto, StatoProdotto.APPROVATO);
    }

    /**
     * Rifiuta un prodotto, aggiornando sia lo stato che il commento.
     */
    public boolean rifiutaProdotto(Prodotto prodotto, String commento) {
        return prodottoDAO.aggiornaStatoECommentoProdotto(prodotto, StatoProdotto.RIFIUTATO, commento);
    }
}
