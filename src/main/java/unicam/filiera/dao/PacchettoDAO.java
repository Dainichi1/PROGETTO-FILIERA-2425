package unicam.filiera.dao;

import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.StatoProdotto;

import java.io.File;
import java.util.List;

public interface PacchettoDAO {
    boolean save(Pacchetto p, List<File> certificati, List<File> foto);

    /**
     * Light update: solo stato e commento
     */
    boolean update(Pacchetto p);

    /**
     * Full update: tutti i campi + re‐upload file
     */
    boolean update(String nomeOriginale,
                   String creatore,
                   Pacchetto p,
                   List<File> certificati,
                   List<File> foto);

    List<Pacchetto> findByCreatore(String creatore);

    List<Pacchetto> findByStato(StatoProdotto stato);

    List<Pacchetto> findAll();

    Pacchetto findByNomeAndCreatore(String nome, String creatore);

    boolean deleteByNomeAndCreatore(String nome, String creatore);
}
