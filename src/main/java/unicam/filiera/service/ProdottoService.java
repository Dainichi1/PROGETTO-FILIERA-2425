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

    /**
     * Permette al Curatore di aggiornare lo stato e il commento di un prodotto.
     *
     * @param nome nome del prodotto
     * @param creatore username del produttore
     * @param nuovoStato nuovo stato (APPROVATO o RIFIUTATO)
     * @param commento eventuale commento (solo in caso di RIFIUTO)
     */
    void cambiaStatoProdotto(String nome, String creatore, StatoProdotto nuovoStato, String commento);
}
