package unicam.filiera.service;

import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.model.ProdottoTrasformato;
import unicam.filiera.model.StatoProdotto;

import java.util.List;

/**
 * Service per la gestione della logica di creazione,
 * modifica e recupero Prodotti Trasformati.
 */
public interface ProdottoTrasformatoService {

    void creaProdottoTrasformato(ProdottoTrasformatoDto dto, String creatore);

    void aggiornaProdottoTrasformato(String nomeOriginale, ProdottoTrasformatoDto dto, String creatore);

    List<ProdottoTrasformato> getProdottiTrasformatiCreatiDa(String creatore);

    List<ProdottoTrasformato> getProdottiTrasformatiByStato(StatoProdotto stato);

    void eliminaProdottoTrasformato(String nome, String creatore);
}
