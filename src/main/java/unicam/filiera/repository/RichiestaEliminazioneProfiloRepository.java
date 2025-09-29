package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unicam.filiera.entity.RichiestaEliminazioneProfiloEntity;
import unicam.filiera.model.StatoRichiestaEliminazioneProfilo;

import java.util.List;
import java.util.Optional;

@Repository
public interface RichiestaEliminazioneProfiloRepository extends JpaRepository<RichiestaEliminazioneProfiloEntity, Long> {

    // tutte le richieste con un certo stato
    List<RichiestaEliminazioneProfiloEntity> findByStato(StatoRichiestaEliminazioneProfilo stato);

    // tutte le richieste fatte da un utente
    List<RichiestaEliminazioneProfiloEntity> findByUsername(String username);

    // richiesta più recente per un utente in un certo stato
    Optional<RichiestaEliminazioneProfiloEntity> findFirstByUsernameAndStatoOrderByDataRichiestaDesc(
            String username,
            StatoRichiestaEliminazioneProfilo stato
    );
}
