package unicam.filiera.dao;

import unicam.filiera.model.RichiestaEliminazioneProfilo;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;

import java.util.List;

public interface RichiestaEliminazioneProfiloDAO {
    boolean save(RichiestaEliminazioneProfilo richiesta);

    boolean updateStato(int richiestaId, StatoRichiestaEliminazioneProfilo nuovoStato);

    List<RichiestaEliminazioneProfilo> findByStato(StatoRichiestaEliminazioneProfilo stato);

    RichiestaEliminazioneProfilo findById(int richiestaId);

    List<RichiestaEliminazioneProfilo> findAll();

    List<RichiestaEliminazioneProfilo> findByUsername(String username);
}
