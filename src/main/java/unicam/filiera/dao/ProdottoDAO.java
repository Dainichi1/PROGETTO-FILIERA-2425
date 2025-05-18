package unicam.filiera.dao;

import unicam.filiera.model.Prodotto;
import unicam.filiera.model.StatoProdotto;

import java.io.File;
import java.util.List;

/**
 * Interfaccia per la gestione della persistenza di Prodotto.
 */
public interface ProdottoDAO {
    /**
     * Salva il prodotto (dettagli + upload file).
     *
     * @param p           dominio senza certificati/foto
     * @param certificati file da copiare in uploads/certificati
     * @param foto        file da copiare in uploads/foto
     */
    boolean save(Prodotto p, List<File> certificati, List<File> foto);

    boolean aggiornaQuantita(String nome, int nuovaQuantita);


    /**
     * Aggiorna SOLO i campi testuali e lo stato/commento.
     * (utile per workflow di approvazione)
     */
    boolean update(Prodotto prodotto);

    /**
     * Aggiorna tutti i campi di un prodotto (anche nome, descrizione, qty, ecc.)
     * e gestisce il re-upload di nuovi certificati/foto.
     *
     * @param nomeOriginale il nome con cui il prodotto era stato salvato originariamente
     * @param creatore      lo username del produttore (chi fa la modifica)
     * @param p             dominio con i nuovi valori di nome, descrizione, quantità, prezzo, indirizzo, stato, commento
     * @param certificati   lista (eventuale) di nuovi file di certificato da caricare
     * @param foto          lista (eventuale) di nuove foto da caricare
     */
    boolean update(
            String nomeOriginale,
            String creatore,
            Prodotto p,
            List<File> certificati,
            List<File> foto
    );

    boolean deleteByNomeAndCreatore(String nome, String creatore);

    List<Prodotto> findByCreatore(String creatore);

    List<Prodotto> findByStato(StatoProdotto stato);

    List<Prodotto> findAll();

    Prodotto findByNome(String nome);

    Prodotto findByNomeAndCreatore(String nome, String creatore);
}
