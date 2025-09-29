package unicam.filiera.service;

import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.entity.ProdottoEntity;
import unicam.filiera.model.StatoProdotto;

import java.util.List;
import java.util.Optional;

public interface ProdottoService {

    void creaProdotto(ProdottoDto dto, String creatore);

    void aggiornaProdotto(Long id, ProdottoDto dto, String creatore);

    List<ProdottoDto> getProdottiCreatiDa(String creatore);

    List<ProdottoDto> getProdottiByStato(StatoProdotto stato);

    void cambiaStatoProdotto(String nome, String creatore, StatoProdotto nuovoStato, String commento);

    List<ProdottoDto> getProdottiApprovatiByProduttore(String usernameProduttore);

    Optional<ProdottoEntity> findEntityById(Long id);

    void eliminaById(Long id, String username);

    Optional<ProdottoEntity> getProdottoById(Long id);

    Optional<ProdottoDto> findDtoById(Long id);

}
