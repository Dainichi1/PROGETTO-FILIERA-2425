package unicam.filiera.controller;

import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MarketplaceController {

    private final ProdottoDAO prodottoDAO;
    private final List<Consumer<List<Prodotto>>> osservatori = new ArrayList<>();

    public MarketplaceController() {
        this.prodottoDAO = new ProdottoDAO();
    }

    public List<Prodotto> ottieniListaProdotti() {
        return prodottoDAO.getProdottiByStato(StatoProdotto.APPROVATO);
    }

    public void registraOsservatore(Consumer<List<Prodotto>> osservatore) {
        osservatori.add(osservatore);
    }

    public void notificaOsservatori() {
        List<Prodotto> prodotti = ottieniListaProdotti();
        for (Consumer<List<Prodotto>> o : osservatori) {
            o.accept(prodotti);
        }
    }

    public Prodotto espandiProdotto(String nomeProdotto) {
        return prodottoDAO.getProdottoByNome(nomeProdotto); // metodo da implementare nel DAO
    }
}
