package unicam.filiera.service;

import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;

import java.util.List;
import java.util.Optional;

public interface ProdottoService {

    void creaProdotto(ProdottoDto dto, String creatore);

    void aggiornaProdotto(Long id, ProdottoDto dto, String creatore);

    List<Prodotto> getProdottiCreatiDa(String creatore);

    List<Prodotto> getProdottiByStato(StatoProdotto stato);


    void cambiaStatoProdotto(String nome, String creatore, StatoProdotto nuovoStato, String commento);

    List<Prodotto> getProdottiApprovatiByProduttore(String usernameProduttore);

    // serve per pre-popolare il form (mantiene nomi file) ===
    Optional<ProdottoEntity> findEntityById(Long id);

    void eliminaById(Long id, String username);

    Optional<ProdottoEntity> getProdottoById(Long id);

}
