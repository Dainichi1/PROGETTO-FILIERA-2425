package unicam.filiera.service;

import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;

import java.util.List;

public interface ProdottoService {

    void creaProdotto(ProdottoDto dto, String creatore);

    void aggiornaProdotto(String nomeOriginale, ProdottoDto dto, String creatore);

    List<Prodotto> getProdottiCreatiDa(String creatore);

    List<Prodotto> getProdottiByStato(StatoProdotto stato);

    void eliminaProdotto(String nome, String creatore);

    void cambiaStatoProdotto(String nome, String creatore, StatoProdotto nuovoStato, String commento);

    // solo prodotti approvati del produttore selezionato
    List<Prodotto> getProdottiApprovatiByProduttore(String usernameProduttore);

    void eliminaProdottoById(Long id, String creatore);
}
