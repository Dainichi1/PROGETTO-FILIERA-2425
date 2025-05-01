package unicam.filiera.dao;

import unicam.filiera.model.Fiera;
import unicam.filiera.model.StatoEvento;
import java.util.List;

public interface FieraDAO {
    boolean save(Fiera fiera);
    boolean update(Fiera fiera);
    List<Fiera> findByOrganizzatore(String organizzatore);
    List<Fiera> findAll();
    Fiera findById(long id);
    List<Fiera> findByStato(StatoEvento stato);
}
