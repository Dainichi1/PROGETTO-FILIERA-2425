package unicam.filiera.service;

import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.entity.ProdottoTrasformatoEntity;
import unicam.filiera.model.StatoProdotto;

import java.util.List;
import java.util.Optional;

public interface ProdottoTrasformatoService {

    void creaProdottoTrasformato(ProdottoTrasformatoDto dto, String creatore);

    void aggiornaProdottoTrasformato(Long id, ProdottoTrasformatoDto dto, String creatore);

    List<ProdottoTrasformatoDto> getProdottiTrasformatiCreatiDa(String creatore);

    List<ProdottoTrasformatoDto> getProdottiTrasformatiByStato(StatoProdotto stato);

    void eliminaProdottoTrasformatoById(Long id, String creatore);

    void cambiaStatoProdottoTrasformato(String nome, String creatore, StatoProdotto nuovoStato, String commento);

    Optional<ProdottoTrasformatoEntity> findEntityById(Long id);

    void eliminaById(Long id, String username);

    Optional<ProdottoTrasformatoDto> findDtoById(Long id);
}
