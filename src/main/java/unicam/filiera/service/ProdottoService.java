package unicam.filiera.service;

import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;

import java.io.File;
import java.util.List;

/**
 * Service per la gestione della logica di creazione,
 * modifica e recupero Prodotti.
 */
public interface ProdottoService {

    /**
     * Crea un nuovo prodotto e lo invia al Curatore.
     */
    void creaProdotto(ProdottoDto dto, String creatore);

    /**
     * Aggiorna tutti i campi di un prodotto precedentemente creato
     * (compreso il nome, i file e lo stato torna IN_ATTESA),
     * identificato dal suo nome originale.
     *
     * @param nomeOriginale il nome con cui il prodotto era stato salvato in precedenza
     * @param dto           i nuovi dati da salvare (nome, descrizione, quantità, prezzo, indirizzo, file…)
     * @param creatore      lo username di chi esegue la modifica
     */
    void aggiornaProdotto(String nomeOriginale, ProdottoDto dto, String creatore);

    /**
     * Recupera tutti i prodotti creati da uno specifico utente.
     */
    List<Prodotto> getProdottiCreatiDa(String creatore);

    /**
     * Recupera tutti i prodotti con un dato stato (IN_ATTESA, RIFIUTATO, APPROVATO…).
     */
    List<Prodotto> getProdottiByStato(StatoProdotto stato);

    /**
     * Elimina un prodotto (solo se è IN_ATTESA o RIFIUTATO).
     */
    void eliminaProdotto(String nome, String creatore);
}
