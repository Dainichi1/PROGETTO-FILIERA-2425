package unicam.filiera.dao;

import unicam.filiera.model.VisitaInvito;
import unicam.filiera.model.StatoEvento;

import java.util.List;

public interface VisitaInvitoDAO {
    boolean save(VisitaInvito v);

    boolean update(VisitaInvito v);

    boolean delete(long id);

    List<VisitaInvito> findByOrganizzatore(String organizzatore);

    List<VisitaInvito> findByStato(StatoEvento stato);

    /**
     * Nuovi metodi per supportare load by id e lista completa
     */
    VisitaInvito findById(long id);

    List<VisitaInvito> findAll();
}
