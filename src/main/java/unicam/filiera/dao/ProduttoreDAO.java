package unicam.filiera.dao;

import unicam.filiera.model.Prodotto;
import unicam.filiera.model.Produttore;
import java.util.List;

public interface ProduttoreDAO {
    List<Produttore> findAll();
    List<Prodotto> findProdottiApprovatiByProduttore(String usernameProduttore);

}
