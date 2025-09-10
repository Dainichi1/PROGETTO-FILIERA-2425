package unicam.filiera.service;

import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.entity.ProdottoTrasformatoEntity;
import unicam.filiera.model.ProdottoTrasformato;
import unicam.filiera.model.StatoProdotto;

import java.util.List;
import java.util.Optional;

public interface ProdottoTrasformatoService {
    void creaProdottoTrasformato(ProdottoTrasformatoDto dto, String creatore);

    void aggiornaProdottoTrasformato(Long id, ProdottoTrasformatoDto dto, String creatore);

    List<ProdottoTrasformato> getProdottiTrasformatiCreatiDa(String creatore);

    List<ProdottoTrasformato> getProdottiTrasformatiByStato(StatoProdotto stato);

    void eliminaProdottoTrasformatoById(Long id, String creatore);

    void cambiaStatoProdottoTrasformato(String nome, String creatore, StatoProdotto nuovoStato, String commento);

    /**
     * Recupera l'entità completa per pre-popolare il form in modifica.
     */
    Optional<ProdottoTrasformatoEntity> findEntityById(Long id);

    void eliminaById(Long id, String username);
}
