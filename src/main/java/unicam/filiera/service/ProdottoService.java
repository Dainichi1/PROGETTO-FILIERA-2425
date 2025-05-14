package unicam.filiera.service;

import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;
import unicam.filiera.model.observer.ProdottoNotifier;
import unicam.filiera.dao.ProdottoDAO;
import unicam.filiera.util.ValidatoreProdotto;

import java.io.File;
import java.util.List;

/**
 * Service per la gestione della logica di creazione e recupero Prodotti.
 */
public interface ProdottoService {
    void creaProdotto(ProdottoDto dto, String creatore);

    List<Prodotto> getProdottiCreatiDa(String creatore);

    List<Prodotto> getProdottiByStato(StatoProdotto stato);

    void eliminaProdotto(String nome, String creatore);
}