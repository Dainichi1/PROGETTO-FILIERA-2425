package unicam.filiera.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unicam.filiera.entity.VisitaInvitoEntity;
import unicam.filiera.model.StatoEvento;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisitaInvitoRepository extends JpaRepository<VisitaInvitoEntity, Long> {

    // tutte le visite create da un utente (animatore)
    List<VisitaInvitoEntity> findByCreatoDa(String creatore);

    // per evitare duplicati: visita con stesso nome e creatore
    Optional<VisitaInvitoEntity> findByNomeAndCreatoDa(String nome, String creatore);

    boolean existsByNomeAndCreatoDa(String nome, String creatore);

    // per recuperare indirizzo a partire dal nome della visita
    @Query("SELECT v.indirizzo FROM VisitaInvitoEntity v " +
            "WHERE LOWER(TRIM(v.nome)) = LOWER(TRIM(:nome))")
    Optional<String> findIndirizzoByNome(@Param("nome") String nome);

    // recupera tutte le visite per stato
    List<VisitaInvitoEntity> findByStato(StatoEvento stato);

    // recupera visite disponibili in base al ruolo destinatario (solo PUBBLICATE)
    @Query("SELECT v FROM VisitaInvitoEntity v " +
            "WHERE v.stato = :stato " +
            "AND LOWER(v.destinatari) LIKE LOWER(CONCAT('%', :ruolo, '%'))")
    List<VisitaInvitoEntity> findByRuoloAndStato(@Param("ruolo") String ruolo,
                                                 @Param("stato") StatoEvento stato);
}
