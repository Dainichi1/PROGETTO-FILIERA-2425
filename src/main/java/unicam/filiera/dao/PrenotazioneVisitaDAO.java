package unicam.filiera.dao;

import unicam.filiera.model.PrenotazioneVisita;
import java.util.List;

public interface PrenotazioneVisitaDAO {
    boolean save(PrenotazioneVisita prenotazione);
    List<PrenotazioneVisita> findByUsername(String usernameVenditore);
    List<PrenotazioneVisita> findByVisita(long idVisita);
    boolean existsByVisitaAndVenditore(long idVisita, String usernameVenditore);
    boolean delete(long idPrenotazione);
    PrenotazioneVisita findById(long idPrenotazione);
}
