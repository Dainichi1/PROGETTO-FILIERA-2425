package unicam.filiera.dao;

import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.StatoProdotto;

import java.io.File;
import java.util.List;

public interface PacchettoDAO {
    boolean saveDetails(Pacchetto p);
    boolean saveFiles(Pacchetto p, List<File> certFiles, List<File> fotoFiles);
    boolean update(Pacchetto p);
    List<Pacchetto> findByCreatore(String creatore);
    List<Pacchetto> findByStato(StatoProdotto stato);
    List<Pacchetto> findAll();
}
