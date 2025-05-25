package unicam.filiera.dao;

import unicam.filiera.model.PrenotazioneFiera;
import java.util.List;

public interface PrenotazioneFieraDAO {
    boolean save(PrenotazioneFiera prenotazione);
    List<PrenotazioneFiera> findByUsername(String username);
    List<PrenotazioneFiera> findByFiera(long idFiera);
    boolean existsByFieraAndAcquirente(long idFiera, String usernameAcquirente);
    boolean delete(long idPrenotazione);
    PrenotazioneFiera findById(long idPrenotazione);


}
