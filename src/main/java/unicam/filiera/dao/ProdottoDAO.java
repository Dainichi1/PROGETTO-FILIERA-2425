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
     * @param p dominio senza certificati/foto
     * @param certificati file da copiare in uploads/certificati
     * @param foto        file da copiare in uploads/foto
     */
    boolean save(Prodotto p, List<File> certificati, List<File> foto);

    boolean update(Prodotto prodotto);
    List<Prodotto> findByCreatore(String creatore);
    List<Prodotto> findByStato(StatoProdotto stato);
    List<Prodotto> findAll();
    Prodotto findByNome(String nome);
}
