package unicam.filiera.controller;

import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;

import java.util.List;

public class MarketplaceController {

    private final ProdottoDAO prodottoDAO;

    public MarketplaceController() {
        this.prodottoDAO = new ProdottoDAO();
    }

    public List<Prodotto> getProdottiApprovati() {
        return prodottoDAO.getProdottiByStato(StatoProdotto.APPROVATO);
    }
}
